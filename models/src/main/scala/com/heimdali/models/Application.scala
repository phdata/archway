package com.heimdali.models

import cats.Show
import io.circe._
import io.circe.syntax._

case class Application(
    name: String,
    consumerGroup: String,
    group: LDAPRegistration,
    applicationType: Option[String] = None,
    logo: Option[String] = None,
    language: Option[String] = None,
    repository: Option[String] = None,
    id: Option[Long] = None
)

object Application {

  implicit val show: Show[Application] =
    Show.show(a => s"${a.name} - ${a.applicationType} (${a.language})")

  implicit val encoder: Encoder[Application] = Encoder.instance { application =>
    Json.obj(
      "id" -> application.id.asJson,
      "name" -> application.name.asJson,
      "consumer_group" -> application.consumerGroup.asJson,
      "group" -> application.group.asJson,
      "application_type" -> application.applicationType.asJson,
      "logo" -> application.logo.asJson,
      "language" -> application.language.asJson,
      "repository" -> application.repository.asJson
    )
  }

  implicit val decoder: Decoder[Application] =
    Decoder.forProduct8("name", "consumer_group", "group", "application_type", "logo", "language", "repository", "id")(
      Application.apply
    )

}
