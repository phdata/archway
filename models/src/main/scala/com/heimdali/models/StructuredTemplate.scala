package com.heimdali.models

import io.circe._
import io.circe.generic.semiauto._

case class StructuredTemplate(name: String, summary: String, description: String, requester: String, compliance: Compliance, includeEnvironment: Boolean, disk: Option[Int], cores: Option[Int], memory: Option[Int])

object StructuredTemplate {
  implicit val encoder: Encoder[StructuredTemplate] = deriveEncoder
  implicit val decoder: Decoder[StructuredTemplate] = deriveDecoder
}