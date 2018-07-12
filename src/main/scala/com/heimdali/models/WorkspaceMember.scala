package com.heimdali.models

import java.time.Instant

import io.circe.{Encoder, Json}
import io.circe.java8.time._
import io.circe.syntax._

case class WorkspaceMember(username: String, created: Option[Instant] = None, id: Option[Long] = None)

object WorkspaceMember {

  implicit val encoder: Encoder[WorkspaceMember] = Encoder.instance { cursor =>
    Json.obj(
      "username" -> cursor.username.asJson,
      "created" -> cursor.created.asJson
    )
  }

}
