package com.heimdali.models

import java.time.Instant

import cats.Show
import io.circe._
import io.circe.generic.semiauto._
import io.circe.java8.time._
import io.circe.syntax._

case class Metadata(
    name: String,
    description: String,
    ordering: Int,
    tags: Map[String, String]
)

object Metadata {
  implicit val metadataEncoder: Encoder[Metadata] = deriveEncoder[Metadata]
  implicit val metadataDecoder: Decoder[Metadata] = deriveDecoder[Metadata]
}

case class UserDN(value: String) {
  assert(value.toLowerCase.contains("cn="), s"Invalid user DN $value")

  override def toString: String = value
}

object UserDN {
  implicit val encoder: Encoder[UserDN] =
    Encoder.instance { userDN =>
      Json.fromString(userDN.value)
    }

  implicit val decoder: Decoder[UserDN] = Decoder.instance { cursor =>
    for {
      value <- cursor.value.as[String]
    } yield (UserDN(value))
  }
}

case class WorkspaceRequest(
    name: String,
    summary: String,
    description: String,
    behavior: String,
    requestedBy: UserDN,
    requestDate: Instant,
    compliance: Compliance,
    singleUser: Boolean,
    id: Option[Long] = None,
    approvals: List[Approval] = List.empty,
    data: List[HiveAllocation] = List.empty,
    processing: List[Yarn] = List.empty,
    applications: List[Application] = List.empty,
    kafkaTopics: List[KafkaTopic] = List.empty,
    metadata: Metadata
) {

  val approved: Boolean = approvals.lengthCompare(2) == 0

  val status: String = if (approved) "Approved" else "Pending"

}

object WorkspaceRequest {

  implicit val show: Show[WorkspaceRequest] =
    Show.show(a => s"""Workspace "${a.name}" (${a.id.getOrElse("[unsaved]")})""")

  implicit val encoder: Encoder[WorkspaceRequest] = Encoder.instance { request =>
    request.approvals.foldLeft(
      Json.obj(
        "id" -> request.id.asJson,
        "summary" -> request.summary.asJson,
        "description" -> request.description.asJson,
        "behavior" -> request.behavior.asJson,
        "name" -> request.name.asJson,
        "compliance" -> request.compliance.asJson,
        "data" -> request.data.asJson,
        "processing" -> request.processing.asJson,
        "applications" -> request.applications.asJson,
        "topics" -> request.kafkaTopics.asJson,
        "single_user" -> request.singleUser.asJson,
        "requester" -> request.requestedBy.asJson,
        "requested_date" -> request.requestDate.asJson,
        "status" -> request.status.asJson,
        "metadata" -> request.metadata.asJson
      )
    )((initial, approvals) => initial deepMerge Json.obj("approvals" -> approvals.asJson))
  }

  implicit def decoder(userDN: UserDN, instant: Instant): Decoder[WorkspaceRequest] = Decoder.instance { json =>
    for {
      name <- json.downField("name").as[String]
      summary <- json.downField("summary").as[String]
      description <- json.downField("description").as[String]
      behavior <- json.downField("behavior").as[String]
      compliance <- json.downField("compliance").as[Compliance]
      singleUser <- json.downField("single_user").as[Boolean]
      data <- json.downField("data").as[List[HiveAllocation]]
      processing <- json.downField("processing").as[List[Yarn]]
      applications <- json.downField("applications").as[List[Application]]
      topics <- json.downField("topics").as[List[KafkaTopic]]
      metadata <- json.downField("metadata").as[Metadata]
    } yield
      WorkspaceRequest(
        name,
        summary,
        description,
        behavior,
        userDN,
        instant,
        compliance,
        singleUser,
        data = data,
        processing = processing,
        applications = applications,
        kafkaTopics = topics,
        metadata = metadata
      )
  }

}
