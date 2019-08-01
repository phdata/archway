package io.phdata.models

import io.circe.Encoder
import io.circe.Decoder

case class MemberRequest(username: String)

object MemberRequest {

  implicit val encoder: Encoder[MemberRequest] =
    Encoder.forProduct1("username")(MemberRequest.unapply)

  implicit val decoder: Decoder[MemberRequest] =
    Decoder.forProduct1("username")(MemberRequest.apply)

}
