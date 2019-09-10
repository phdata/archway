package io.phdata.models

import java.time.Instant

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._

case class ComplianceQuestion(
    question: String,
    requester: String,
    updated: Instant,
    complianceGroupId: Option[Long] = None,
    id: Option[Long] = None
)

object ComplianceQuestion {

  implicit val encoder: Encoder[ComplianceQuestion] =
    Encoder.instance { q =>
      Json.obj(
        "question" -> q.question.asJson,
        "requester" -> q.requester.asJson,
        "updated" -> q.updated.asJson,
        "complianceGroupId" -> q.complianceGroupId.asJson,
        "id" -> q.id.asJson
      )
    }

  implicit def decoder(instant: Instant): Decoder[ComplianceQuestion] =
    Decoder.instance { q =>
      for {
        question <- q.downField("question").as[String]
        requester <- q.downField("requester").as[String]
        complianceGroupId <- q.downField("complianceGroupId").as[Option[Long]]
        id <- q.downField("id").as[Option[Long]]
      } yield ComplianceQuestion(question, requester, instant, complianceGroupId, id)
    }
}
