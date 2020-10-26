package io.phdata.models

import io.circe._
import io.circe.syntax._

case class WorkspaceMemberEntry(
    distinguishedName: String,
    name: String,
    email: Option[String],
    data: List[MemberRights]
)

object WorkspaceMemberEntry {

  implicit val encoder: Encoder[WorkspaceMemberEntry] =
    Encoder.instance { m =>
      Json.obj(
        "distinguished_name" -> m.distinguishedName.asJson,
        "name" -> m.name.asJson,
        "email" -> m.email.asJson,
        "data" -> Json.obj(m.data.map(d => d.name -> d.asJson): _*)
      )
    }

}
