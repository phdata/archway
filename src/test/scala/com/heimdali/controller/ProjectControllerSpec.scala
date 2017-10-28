package com.heimdali.controller

import java.nio.file.Files
import java.time.LocalDateTime
import javax.inject.Inject

import com.heimdali.models.{Compliance, HDFSProvision, Project}
import com.heimdali.services._
import com.heimdali.startup.{SecurityContext, Startup}
import com.heimdali.test.fixtures._
import com.unboundid.ldap.sdk.SearchScope
import io.getquill._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.hdfs.MiniDFSCluster
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers}
import org.scalatestplus.play.{BaseOneAppPerSuite, FakeApplicationFactory}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.functional.syntax._
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._
import play.api.libs.json._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class ProjectControllerSpec
  extends FlatSpec
    with Matchers
    with BeforeAndAfterAll
    with LDAPTest
    with BaseOneAppPerSuite
    with FakeApplicationFactory
    with BeforeAndAfterEach {

  behavior of "ProjectController"

  it should "create a project" in {
    val json = Json.obj(
      "name" -> "Sesame",
      "purpose" -> "to do something cool",
      "compliance" -> Json.obj(
        "pii_data" -> false,
        "phi_data" -> false,
        "pci_data" -> false
      ),
      "hdfs" -> Json.obj(
        "requested_gb" -> .2
      )
    )

    val request = FakeRequest(POST, "/projects")
      .withHeaders(AUTHORIZATION -> "Bearer AbCdEf123456")
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(json)

    val rootCall = route(app, request).get

    status(rootCall) should be(CREATED)

    val jsonResponse = contentAsJson(rootCall).as[JsObject]

    (jsonResponse \ "id").asOpt[Long] shouldBe defined
    val id = (jsonResponse \ "id").as[Long]
    (jsonResponse \ "name").as[String] should be("Sesame")
    (jsonResponse \ "compliance" \ "pci_data").as[Boolean] should be(false)
    (jsonResponse \ "compliance" \ "pii_data").as[Boolean] should be(false)
    (jsonResponse \ "compliance" \ "phi_data").as[Boolean] should be(false)
    (jsonResponse \ "purpose").as[String] should be("to do something cool")
    (jsonResponse \ "system_name").as[String] should be("sesame")

    val date = (jsonResponse \ "created").asOpt[DateTime]
    date shouldBe defined

    implicit val dateOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
    date.get should be < DateTime.now

    val creator = (jsonResponse \ "created_by").asOpt[String]
    creator shouldBe defined
    creator.get shouldBe "username"

    Thread.sleep((5 seconds).toMillis) // let the actor system update LDAP and HDFS

    val result = db.find(id)
    result.ldapDn shouldBe defined
    result.hdfs.location shouldBe defined
    result.keytabLocation shouldBe defined
  }

  it should "not accept read-only fields" in {
    val oldDate = new DateTime(2010, 1, 1, 0, 0, 0)
    val wrongUser = "johnsmith"
    val fakeId = 999
    val json = Json.obj(
      "id" -> fakeId,
      "name" -> "sesame",
      "purpose" -> "to do something cool",
      "system_name" -> "blahblah",
      "compliance" -> Json.obj(
        "pii_data" -> false,
        "phi_data" -> false,
        "pci_data" -> false
      ),
      "hdfs" -> Json.obj(
        "requested_gb" -> .01
      ),
      "created" -> oldDate,
      "created_by" -> wrongUser
    )

    val request = FakeRequest(POST, "/projects")
      .withHeaders(AUTHORIZATION -> "Bearer AbCdEf123456")
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(json)

    val rootCall = route(app, request).get

    status(rootCall) should be(CREATED)

    val jsonResponse = contentAsJson(rootCall).as[JsObject]

    (jsonResponse \ "id").as[Int] should not be fakeId
    (jsonResponse \ "created").as[DateTime] should not be oldDate
    (jsonResponse \ "created_by").as[String] should not be wrongUser
    (jsonResponse \ "system_name").as[String] should be("sesame")
  }

  it should "list all projects" in {
    val Array(project1, project2) = Array(
      db.load(TestProject(id = 123L, name = "Project 1", createdBy = "username")),
      db.load(TestProject(id = 321L, name = "Project 2"))
    )

    val request = FakeRequest(GET, "/projects")
      .withHeaders(AUTHORIZATION -> "Bearer AbCdEf123456")

    val rootCall = route(app, request).get

    status(rootCall) should be(OK)

    val jsonResponse = contentAsJson(rootCall).as[Seq[JsObject]]
    jsonResponse.size should be(1)
    (jsonResponse.head \ "id").as[Long] should be(project1.id)
  }

  import play.api.inject.bind

  val cluster = {
    val conf = new Configuration
    conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, Files.createTempDirectory("test_hdfs").toFile.getAbsoluteFile.getAbsolutePath)
    new MiniDFSCluster.Builder(conf).build()
  }

  override val fakeApplication: Application =
    new GuiceApplicationBuilder()
      .overrides(bind[AccountService].to[PassiveAccountService])
      .overrides(bind[FileSystem].toInstance(cluster.getFileSystem))
      .overrides(bind[KeytabService].to[FakeKeytabService])
      .overrides(bind[Startup].to[TestStartup])
      .build()

  override protected def afterEach(): Unit = {
    import scala.collection.JavaConverters._
    db.clear
    val users = ldapConnection.search("ou=users,ou=hadoop,dc=jotunn,dc=io", SearchScope.SUB, "(objectClass=person)").getSearchEntries.asScala
    val groups = ldapConnection.search("ou=groups,ou=hadoop,dc=jotunn,dc=io", SearchScope.SUB, "(objectClass=groupOfNames)").getSearchEntries.asScala
    (users ++ groups).map(_.getDN).map(ldapConnection.delete)
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val db = new DB
}

class DB(implicit executionContext: ExecutionContext) {
  lazy val ctx = new PostgresAsyncContext[SnakeCase with PluralizedTableNames]("ctx") with ImplicitQuery

  import ctx._

  val projectQuery = quote {
    querySchema[Project](
      "projects",
      _.compliance.pciData -> "pci_data",
      _.compliance.phiData -> "phi_data",
      _.compliance.piiData -> "pii_data",
      _.hdfs.location -> "hdfs_location",
      _.hdfs.requestedSizeInGB -> "hdfs_requested_size_in_gb"
    )
  }

  def load(project: Project) =
    project.copy(id = Await.result(run(projectQuery.insert(lift(project)).returning(_.id)), Duration.Inf))

  def clear =
    Await.ready(run(projectQuery.delete), Duration.Inf)

  def find(id: Long) =
    Await.result(run(projectQuery.filter(_.id == lift(id))), Duration.Inf).head
}
