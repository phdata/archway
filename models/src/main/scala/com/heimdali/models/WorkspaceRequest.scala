package com.heimdali.models

import java.time.Instant

import cats.effect.Clock
import cats.implicits._
import cats.effect.implicits._
import io.circe._
import io.circe.java8.time._
import io.circe.syntax._

case class WorkspaceRequest(name: String,
                            summary: String,
                            description: String,
                            behavior: String,
                            requestedBy: String,
                            requestDate: Instant,
                            compliance: Compliance,
                            singleUser: Boolean,
                            id: Option[Long] = None,
                            approvals: List[Approval] = List.empty,
                            data: List[HiveAllocation] = List.empty,
                            processing: List[Yarn] = List.empty,
                            applications: List[Application] = List.empty,
                            kafkaTopics: List[KafkaTopic] = List.empty) {

  val approved: Boolean = approvals.lengthCompare(2) == 0

  val status: String = if(approved) "Approved" else "Pending"

}

object WorkspaceRequest {

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
        "status" -> request.status.asJson
      )
    )((initial, approvals) => initial deepMerge Json.obj("approvals" -> approvals.asJson))
  }

  implicit def decoder(user: User, instant: Instant): Decoder[WorkspaceRequest] = Decoder.instance { json =>
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
    } yield WorkspaceRequest(name, summary, description, behavior, user.distinguishedName, instant, compliance, singleUser, data = data, processing = processing, applications = applications, kafkaTopics = topics)
  }

}
