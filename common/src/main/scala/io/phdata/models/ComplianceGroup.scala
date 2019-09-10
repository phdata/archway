package io.phdata.models

import java.time.Instant

import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

case class ComplianceGroup(
    name: String,
    description: String,
    questions: List[ComplianceQuestion],
    id: Option[Long] = None
) {
  assert(name.nonEmpty)
  assert(description.nonEmpty)
}

object ComplianceGroup {

  implicit val encoder: Encoder[ComplianceGroup] =
    Encoder.instance { g =>
      Json.obj(
        "id" -> g.id.asJson,
        "name" -> g.name.asJson,
        "description" -> g.description.asJson,
        "questions" -> g.questions.asJson
      )
    }

  implicit def decoder(instant: Instant): Decoder[ComplianceGroup] = {
    implicit val complianceQuestionDecoder = ComplianceQuestion.decoder(instant)

    Decoder.instance { g =>
      for {
        id <- g.downField("compliance").downField("id").as[Option[Long]]
        name <- g.downField("compliance").downField("name").as[String]
        description <- g.downField("compliance").downField("description").as[String]
        questions <- g.downField("compliance").downField("questions").as[List[ComplianceQuestion]]
      } yield ComplianceGroup(name, description, questions, id)
    }
  }

  def defaultGroups(instant: Instant): List[ComplianceGroup] = {
    List(
      ComplianceGroup(
        "PCI",
        "Payment Card Industry [Data Security Standard",
        List(
          ComplianceQuestion("Full or partial credit card numbers?", "manager", instant),
          ComplianceQuestion("Full or partial bank account numbers?", "manager", instant),
          ComplianceQuestion("Any other combination of data that can be used to make purchases?", "manager", instant)
        )
      ),
      ComplianceGroup(
        "PII",
        "Personally Identifiable Information",
        List(
          ComplianceQuestion("Full name", "manager", instant),
          ComplianceQuestion("Home address", "manager", instant),
          ComplianceQuestion("Email address", "manager", instant),
          ComplianceQuestion("Social security number", "manager", instant),
          ComplianceQuestion("Passport number", "manager", instant),
          ComplianceQuestion("Driver's license number", "manager", instant),
          ComplianceQuestion("Credit card number", "manager", instant),
          ComplianceQuestion("Date of birth", "manager", instant),
          ComplianceQuestion("Telephone number", "manager", instant),
          ComplianceQuestion("Software credentials", "manager", instant)
        )
      ),
      ComplianceGroup(
        "PHI",
        "Protected Health Information",
        List(
          ComplianceQuestion("Health status", "manager", instant),
          ComplianceQuestion("Provision of health care", "manager", instant),
          ComplianceQuestion("Payment for health care", "manager", instant)
        )
      )
    )
  }

}
