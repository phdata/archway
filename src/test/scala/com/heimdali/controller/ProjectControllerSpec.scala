package com.heimdali.controller

import java.time.LocalDateTime

import com.heimdali.models.Project
import com.heimdali.services._
import io.getquill._
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers}
import org.scalatestplus.play.{BaseOneAppPerSuite, FakeApplicationFactory}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._
import play.api.libs.json._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.libs.functional.syntax._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class ProjectControllerSpec
  extends FlatSpec
    with Matchers
    with BeforeAndAfterAll
    with BaseOneAppPerSuite
    with FakeApplicationFactory
    with BeforeAndAfterEach {

  behavior of "ProjectController"

  it should "create a project" in {
    val json = Json.obj(
      "name" -> "sesame",
      "purpose" -> "to do something cool"
    )

    val request = FakeRequest(POST, "/projects")
      .withHeaders(AUTHORIZATION -> "Bearer AbCdEf123456")
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(json)

    val rootCall = route(app, request).get

    status(rootCall) should be(CREATED)

    val jsonResponse = contentAsJson(rootCall).as[JsObject]

    (jsonResponse \ "id").asOpt[Int] shouldBe defined
    (jsonResponse \ "name").as[String] should be("sesame")
    (jsonResponse \ "purpose").as[String] should be("to do something cool")

    val date = (jsonResponse \ "created").asOpt[DateTime]
    date shouldBe defined

    implicit val dateOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
    date.get should be < DateTime.now

    val creator = (jsonResponse \ "created_by").asOpt[String]
    creator shouldBe defined
    creator.get shouldBe "username"
  }

  it should "not accept read-only fields" in {
    val oldDate = new DateTime(2010, 1, 1, 0, 0, 0)
    val wrongUser = "johnsmith"
    val fakeId = 999
    val json = Json.obj(
      "id" -> fakeId,
      "name" -> "sesame",
      "purpose" -> "to do something cool",
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
  }

  it should "list all projects" in {
    val Array(project1, project2) = Array(
      db.load(Project(123L, "Project 1", "Stuff", LocalDateTime.now(), "username")),
      db.load(Project(321L, "Project 2", "Stuff", LocalDateTime.now(), "someone"))
    )

    val request = FakeRequest(GET, "/projects")
      .withHeaders(AUTHORIZATION -> "Bearer AbCdEf123456")

    val rootCall = route(app, request).get

    status(rootCall) should be(OK)

    implicit val projectRead = (
      (__ \ "id").read[Long] ~
        (__ \ "name").read[String] ~
        (__ \ "purpose").read[String] ~
        (__ \ "created").read[LocalDateTime] ~
        (__ \ "created_by").read[String]
      )(Project.apply _)
    val jsonResponse = contentAsJson(rootCall).as[Seq[Project]]
    jsonResponse.size should be(1)
    jsonResponse.head should be(project1)
  }

  import play.api.inject.bind

  override val fakeApplication: Application =
    new GuiceApplicationBuilder()
      .overrides(bind[AccountService].to[PassiveAccountService])
      .build()

  override protected def afterEach(): Unit =
    db.clear

  import scala.concurrent.ExecutionContext.Implicits.global
  lazy val db = new DB
}

class DB (implicit executionContext: ExecutionContext) {
  lazy val ctx = new PostgresAsyncContext[SnakeCase with PluralizedTableNames]("ctx") with ImplicitQuery
  import ctx._

  def load(project: Project) =
    project.copy(id = Await.result(run(query[Project].insert(lift(project)).returning(_.id)), Duration.Inf))

  def clear =
    Await.ready(run(query[Project].delete), Duration.Inf)
}