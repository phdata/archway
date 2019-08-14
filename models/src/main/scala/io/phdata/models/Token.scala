package io.phdata.models

import io.circe._
import io.circe.syntax._

case class Token(accessToken: String, refreshToken: String)

object Token {
  implicit val encoder: Encoder[Token] =
    Encoder.instance(
      token =>
        Json.obj(
          "access_token" -> token.accessToken.asJson,
          "refresh_token" -> token.refreshToken.asJson
        )
    )
}
