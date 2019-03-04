package com.heimdali.models

import io.circe._
import io.circe.generic.semiauto._

case class SimpleTemplate(name: String, summary: String, description: String, requester: String, compliance: Compliance, disk: Option[Int], cores: Option[Int], memory: Option[Int])

object SimpleTemplate {
  implicit val encoder: Encoder[SimpleTemplate] = deriveEncoder
  implicit val decoder: Decoder[SimpleTemplate] = deriveDecoder
}
