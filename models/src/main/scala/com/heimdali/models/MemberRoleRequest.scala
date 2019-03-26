package com.heimdali.models

import io.circe._

case class MemberRoleRequest(distinguishedName: String, resource: String, resourceId: Long, role: Option[DatabaseRole])

object MemberRoleRequest {

  implicit val decoder: Decoder[MemberRoleRequest] =
    Decoder.instance { r =>
      for {
        dn <- r.downField("distinguished_name").as[String]
        resource <- r.downField("resource").as[String]
        resourceId <- r.downField("resource_id").as[Long]
        role <- r.downField("role").as[String]
      } yield MemberRoleRequest(dn, resource, resourceId, DatabaseRole.unapply(role))
    }

  implicit val minDecoder: Decoder[MemberRoleRequest] =
    Decoder.instance { r =>
      for {
        dn <- r.downField("distinguished_name").as[String]
        resource <- r.downField("resource").as[String]
        resourceId <- r.downField("resource_id").as[Long]
      } yield MemberRoleRequest(dn, resource, resourceId, None)
    }

}