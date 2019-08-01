package io.phdata.services

import io.circe._

case class ApplicationRequest(
    name: String,
    applicationType: Option[String] = None,
    logo: Option[String] = None,
    language: Option[String] = None,
    repository: Option[String] = None
)

object ApplicationRequest {

  implicit val encoder: Decoder[ApplicationRequest] =
    Decoder.forProduct5("name", "application_type", "logo", "language", "repository")(ApplicationRequest.apply)

}
