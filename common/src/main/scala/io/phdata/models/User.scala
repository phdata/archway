package io.phdata.models

import cats.Show
import cats.implicits._
import io.circe.{Decoder, Encoder}

case class User(
    name: String,
    username: String,
    distinguishedName: DistinguishedName,
    permissions: UserPermissions = UserPermissions(riskManagement = false, platformOperations = false)
) {
  val canApprove: Boolean = permissions.platformOperations || permissions.riskManagement

  val role: ApproverRole = permissions match {
    case UserPermissions(true, false) => Risk
    case UserPermissions(false, true) => Infra
    case UserPermissions(true, true)  => Full
    case _                            => NA
  }

  val isSuperUser: Boolean = permissions.platformOperations && permissions.riskManagement
  val isOpsUser: Boolean = permissions.platformOperations
  val isRiskUser: Boolean = permissions.riskManagement
}

object User {

  implicit val encoder: Encoder[User] =
    Encoder.forProduct4("name", "username", "distinguished_name", "permissions")(
      m => (m.name, m.username, m.distinguishedName, m.permissions)
    )

  implicit val decoder: Decoder[User] =
    Decoder.forProduct4("name", "username", "distinguished_name", "permissions")(User.apply)

  implicit val shower: Show[User] =
    Show.show(user => s"${user.name} (${user.role.show}")
}
