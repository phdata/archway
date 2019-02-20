package com.heimdali.services

import io.circe._

case class ApplicationRequest(name: String)

object ApplicationRequest {

  implicit val encoder: Decoder[ApplicationRequest] =
    Decoder.instance { json =>
      for {
        name <- json.downField("name").as[String]
      } yield ApplicationRequest(name)
    }

}