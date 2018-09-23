package com.heimdali.models

import io.circe._
import io.circe.syntax._

case class WorkspaceMemberEntry(username: String, name: String, email: Option[String], data: List[MemberRights], processing: List[MemberRights], topics: List[MemberRights], applications: List[MemberRights])

object WorkspaceMemberEntry {

  implicit val encoder: Encoder[WorkspaceMemberEntry] =
    Encoder.instance { m =>
      Json.obj(
        "username" -> m.username.asJson,
        "name" -> m.name.asJson,
        "email" -> m.email.asJson,
        "data" -> Json.obj(m.data.map(d => d.name -> d.asJson):_*),
        "processing" -> Json.obj(m.processing.map(d => d.name -> d.asJson):_*),
        "topics" -> Json.obj(m.topics.map(d => d.name -> d.asJson):_*),
        "applications" -> Json.obj(m.applications.map(d => d.name -> d.asJson):_*)
      )
    }

}