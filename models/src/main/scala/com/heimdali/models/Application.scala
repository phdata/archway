package com.heimdali.models

import io.circe._
import io.circe.syntax._

case class Application(name: String,
                       consumerGroup: String,
                       group: LDAPRegistration,
                       id: Option[Long] = None)

object Application {

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
