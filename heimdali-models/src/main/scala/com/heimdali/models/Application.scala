package com.heimdali.models

import cats.effect.Effect
import cats.implicits._
import io.circe._
import io.circe.syntax._

case class Application(name: String,
                       consumerGroup: String,
                       group: LDAPRegistration,
                       id: Option[Long] = None,
                       requestor: Option[String] = None)

object Application {

  def apply(requestor: String, workspaceName: String, name: String, groupDN: String): Application = {
    val consumerGroup = s"${workspaceName}_${name}_cg"
    Application(
      name,
      consumerGroup,
      LDAPRegistration(s"cn=$consumerGroup,$groupDN", consumerGroup, s"role_$consumerGroup"),
      requestor = Some(requestor)
    )
  }

  implicit val encoder: Encoder[Application] = Encoder.instance { application =>
    Json.obj(
      "id" -> application.id.asJson,
      "name" -> application.name.asJson,
      "consumer_group" -> application.consumerGroup.asJson,
      "group" -> application.group.asJson
    )
  }

  implicit val decoder: Decoder[Application] =
    Decoder.forProduct4("id", "name", "group", "consumer_group")((id: Option[Long], name: String, group: LDAPRegistration, consumer: String) => Application(name, consumer, group, id))


}
