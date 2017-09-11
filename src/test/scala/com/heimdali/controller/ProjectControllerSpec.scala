package com.heimdali.controller

import com.heimdali.services._
import org.flywaydb.core.Flyway
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.scalatestplus.play.{BaseOneAppPerSuite, FakeApplicationFactory}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._
import play.api.libs.json._
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ProjectControllerSpec
  extends FlatSpec
    with Matchers
    with BeforeAndAfterAll
    with BaseOneAppPerSuite
    with FakeApplicationFactory {

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

  import play.api.inject.bind

  override val fakeApplication: Application =
    new GuiceApplicationBuilder()
      .overrides(bind[AccountService].to[PassiveAccountService])
      .build()

  override protected def beforeAll(): Unit = {
    val flyway = new Flyway()
    flyway.setDataSource("jdbc:postgresql://localhost:5432/heimdali", "postgres", "postgres")
    flyway.setValidateOnMigrate(false)
    flyway.migrate()
  }
}