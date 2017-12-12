package com.heimdali.controller

import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.heimdali.HeimdaliAPI
import io.circe.parser._
import com.heimdali.models.{Compliance, HDFSProvision, Project}
import com.heimdali.services._
import com.heimdali.test.fixtures._
import com.unboundid.ldap.sdk.SearchScope
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.getquill.{ImplicitQuery, PluralizedTableNames, PostgresAsyncContext, SnakeCase}
import jawn.Parser._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hdfs.MiniDFSCluster
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class ProjectControllerSpec
  extends FlatSpec
    with Matchers
    with BeforeAndAfterAll
    with LDAPTest
    with MockFactory
    with ScalatestRouteTest
    with FailFastCirceSupport
    with BeforeAndAfterEach {

  import io.circe.java8.time._
  import io.circe.generic.auto._

  behavior of "ProjectController"

  it should "create a project" in {
    val json = parse(
      """
      | {
      |   "name": "Sesame",
      |   "purpose": "to do something cool",
      |   "compliance": {
      |     "pii_data": false,
      |     "phi_data": false,
      |     "pci_data": false
      |   },
      |   "hdfs": {
      |     "requested_gb": 0.2
      |   }
      | }
    """.stripMargin)

    val clusterService = mock[ClusterService]
    val projectService = mock[ProjectService]
    (projectService.create _).expects(*).returning(Future(Project(123, "", "", Some(""), "", Compliance(true, true, true), HDFSProvision(None, 0.2), None, LocalDateTime.now(), "")))
    val accountService = mock[AccountService]
    (accountService.validate _).expects("AbCdEf123456").returning(Future(Some(User("", ""))))
    val restApi = new HeimdaliAPI(clusterService, projectService, accountService)

    Post("/workspaces", json) ~> addCredentials(OAuth2BearerToken("AbCdEf123456")) ~> restApi.route ~> check {
      status should be(201)
      val response = responseAs[Project]

      val id = response.id
      response.name should be("Sesame")
      response.purpose should be("to do something cool")
      response.systemName should be("sesame")

      response.compliance.piiData should be(false)
      response.compliance.phiData should be(false)
      response.compliance.pciData should be(false)

      implicit val dateOrdering: Ordering[LocalDateTime] = Ordering.fromLessThan(_ isBefore _)
      response.created should be < LocalDateTime.now

      response.createdBy should be("username")

      Thread.sleep((5 seconds).toMillis) // let the actor system update LDAP and HDFS

      val result = db.find(id)
      result.ldapDn shouldBe defined
      result.hdfs.location shouldBe defined
      result.keytabLocation shouldBe defined
    }
  }

  it should "not accept read-only fields" in {
    val oldDate = LocalDateTime.of(2010, 1, 1, 0, 0, 0)
    val oldDateString = oldDate.format(DateTimeFormatter.ISO_DATE_TIME)
    val wrongUser = "johnsmith"
    val fakeId = 999
    val json = parse(
      s"""{
      "id": $fakeId,
      "name": "sesame",
      "purpose": "to do something cool",
      "system_name": "blahblah",
      "compliance": {
        "pii_data"" false,
        "phi_data": false,
        "pci_data": false
      },
      "hdfs": {
        "requested_gb": 0.01
      },
      "created": $oldDateString,
      "created_by": $wrongUser
      }""")


    val clusterService = mock[ClusterService]
    val projectService = mock[ProjectService]
    val accountService = mock[AccountService]
    val restApi = new HeimdaliAPI(clusterService, projectService, accountService)

    Post("/projects", json) ~> addCredentials(OAuth2BearerToken("AbCdEf123456")) ~> restApi.route ~> check {
      status should be(201)

      val result = responseAs[Project]
      result.id should not be fakeId
      result.created should not be oldDate
      result.createdBy should not be wrongUser
      result.systemName should not be "blahblah"
    }
  }

  it should "list all projects" in {
    val projects@Seq(project1, project2) = Seq(
      db.load(TestProject(id = 123L, name = "Project 1", createdBy = "username")),
      db.load(TestProject(id = 321L, name = "Project 2"))
    )

    val clusterService = mock[ClusterService]
    val accountService = mock[AccountService]
    val projectService = mock[ProjectService]
    (projectService.list _).expects("username").returning(Future(projects))

    val restApi = new HeimdaliAPI(clusterService, projectService, accountService)

    Get("/projects") ~> addCredentials(OAuth2BearerToken("AbCdEf123456")) ~> restApi.route ~> check {
      status should be(200)
      val result = responseAs[Seq[Project]]
      result.size should be(1)
      result.head.id should be(project1.id)
    }
  }

  val cluster = {
    val conf = new Configuration
    conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, Files.createTempDirectory("test_hdfs").toFile.getAbsoluteFile.getAbsolutePath)
    new MiniDFSCluster.Builder(conf).build()
  }

  override protected def afterEach(): Unit = {
    import scala.collection.JavaConverters._
    db.clear
    val users = ldapConnection.search("ou=users,ou=hadoop,dc=jotunn,dc=io", SearchScope.SUB, "(objectClass=person)").getSearchEntries.asScala
    val groups = ldapConnection.search("ou=groups,ou=hadoop,dc=jotunn,dc=io", SearchScope.SUB, "(objectClass=groupOfNames)").getSearchEntries.asScala
    (users ++ groups).map(_.getDN).map(ldapConnection.delete)
  }

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
