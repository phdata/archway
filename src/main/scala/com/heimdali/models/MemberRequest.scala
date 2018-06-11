package com.heimdali.models

import io.circe.Encoder
import io.circe.Decoder

case class MemberRequest(username: String)

object MemberRequest {
  import io.circe.generic.semiauto._

  implicit val encoder: Encoder[MemberRequest] = deriveEncoder
  implicit val decoder: Decoder[MemberRequest] = deriveDecoder

}