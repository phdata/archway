package com.heimdali.models

import io.circe.generic.extras.Configuration
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class User(name: String, username: String, distinguishedName: String, permissions: UserPermissions = UserPermissions(riskManagement = false, platformOperations = false)) {
  val canApprove: Boolean = permissions.platformOperations || permissions.riskManagement
  val role: ApproverRole = permissions match {
    case UserPermissions(true, false) => Risk
    case UserPermissions(false, true) => Infra
    case _ => NA
  }

  val isSuperUser: Boolean = permissions.platformOperations && permissions.riskManagement
}

object User {

  implicit val encoder: Encoder[User] =
    Encoder.forProduct4("name", "username", "distinguished_name", "permissions")(m => (m.name, m.username, m.distinguishedName, m.permissions))

  implicit val decoder: Decoder[User] =
    Decoder.forProduct4("name", "username", "distinguished_name", "permissions")(User.apply)
}
