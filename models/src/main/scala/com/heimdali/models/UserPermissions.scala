package com.heimdali.models

import io.circe.{Decoder, Encoder}

case class UserPermissions(riskManagement: Boolean, platformOperations: Boolean)

object UserPermissions {

  implicit val encoder: Encoder[UserPermissions] =
    Encoder.forProduct2("risk_management", "platform_operations")(up => (up.riskManagement, up.platformOperations))

  implicit val decoder: Decoder[UserPermissions] =
    Decoder.forProduct2("risk_management", "platform_operations")(UserPermissions.apply)

}
