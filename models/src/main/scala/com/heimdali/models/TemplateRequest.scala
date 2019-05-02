package com.heimdali.models

import io.circe.{Decoder, Encoder}

case class TemplateRequest(name: String, summary: String, description: String, compliance: Compliance, requester: String) {
  val generatedName: String =
    name
      .replaceAll("""[^a-zA-Z\d\s]""", " ") //replace with space to remove multiple underscores
      .trim //if we have leading or trailing spaces, clean them up
      .replaceAll("""\s+""", "_")
      .toLowerCase
}

object TemplateRequest {

  implicit val encoder: Encoder[TemplateRequest] =
    Encoder.forProduct5("name", "summary", "description", "compliance", "requester")(
      r => (r.name, r.summary, r.description, r.compliance, r.requester)
    )

  implicit val decoder: Decoder[TemplateRequest] =
    Decoder.forProduct5("name", "summary", "description", "compliance", "requester")(TemplateRequest.apply)

}