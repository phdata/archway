package com.heimdali.models

import io.circe.generic.extras.Configuration
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class User(name: String, username: String, permissions: UserPermissions = UserPermissions(riskManagement = false, platformOperations = false))

object User {
  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
  implicit val encoder: Encoder[User] = deriveEncoder
  implicit val decoder: Decoder[User] = deriveDecoder
}