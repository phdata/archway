package com.heimdali.models

import io.circe.Encoder
import java.time.Instant

import io.circe.Json
import io.circe.syntax._
import io.circe.generic.semiauto._
import io.circe.java8.time._

case class WorkspaceMember(username: String, created: Option[Instant] = None, id: Option[Long] = None)

object WorkspaceMember {

  implicit val encoder: Encoder[WorkspaceMember] = Encoder.instance { cursor =>
    Json.obj(
      "username" -> cursor.username.asJson,
      "created" -> cursor.created.asJson
    )
  }

}
