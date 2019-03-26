package com.heimdali.models

import io.circe._
import io.circe.generic.semiauto._

case class UserTemplate(userDN: String, username: String, disk: Option[Int], cores: Option[Int], memory: Option[Int])

object UserTemplate {
  implicit val encoder: Encoder[UserTemplate] = deriveEncoder
  implicit val decoder: Decoder[UserTemplate] = deriveDecoder
}
