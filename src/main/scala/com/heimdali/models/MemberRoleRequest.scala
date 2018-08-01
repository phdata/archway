package com.heimdali.models

import com.heimdali.repositories.DatabaseRole
import io.circe._

case class MemberRoleRequest(username: String, resource: String, resourceId: Long, role: DatabaseRole)

object MemberRoleRequest {

  implicit val decoder: Decoder[MemberRoleRequest] =
    Decoder.instance { r =>
      for {
        username <- r.downField("username").as[String]
        resource <- r.downField("resource").as[String]
        resourceId <- r.downField("resource_id").as[Long]
        role <- r.downField("role").as[String]
      } yield MemberRoleRequest(username, resource, resourceId, DatabaseRole.unapply(role).get)
    }

}