package io.phdata.models

import java.time.Instant

import io.circe.{Decoder, Encoder}

case class TemplateRequest(
    name: String,
    summary: String,
    description: String,
    compliance: List[ComplianceQuestion],
    requester: DistinguishedName,
    generateNextId: Boolean = true
) {
  val generatedName: String = TemplateRequest.generateName(name)
}

object TemplateRequest {

  def generateName(name: String): String =
    name
      .replaceAll("""[^a-zA-Z\d\s]""", " ") //replace with space to remove multiple underscores
      .trim //if we have leading or trailing spaces, clean them up
      .replaceAll("""\s+""", "_")
      .toLowerCase

  implicit val encoder: Encoder[TemplateRequest] =
    Encoder.forProduct5("name", "summary", "description", "compliance", "requester")(
      r => (r.name, r.summary, r.description, r.compliance, r.requester)
    )

  implicit def decoder(instant: Instant): Decoder[TemplateRequest] = {
    implicit val complianceQuestionDecoder = ComplianceQuestion.decoder(instant)
    Decoder.instance { cursor =>
      for {
        name <- cursor.downField("name").as[String]
        summary <- cursor.downField("summary").as[String]
        description <- cursor.downField("description").as[String]
        compliance <- cursor.downField("compliance").as[List[ComplianceQuestion]]
        requester <- cursor.downField("requester").as[DistinguishedName]
      } yield TemplateRequest(name, summary, description, compliance, requester)
    }
  }
}
