package com.heimdali.models

import java.time.{Clock, Instant}

import cats.implicits._
import cats.data.Kleisli
import cats.effect.Effect
import io.circe._
import io.circe.java8.time._
import io.circe.syntax._
import doobie._
import doobie.implicits._

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

  implicit val workspaceRequestComposite: Composite[WorkspaceRequest] =
    Composite[(String, String, String, String, String, Instant, Boolean, Boolean, Boolean, Option[Long], Boolean, Option[Long])].imap(
      (t: (String, String, String, String, String, Instant, Boolean, Boolean, Boolean, Option[Long], Boolean, Option[Long])) =>
        WorkspaceRequest(t._1, t._2, t._3, t._4, t._5, t._6, Compliance(t._7, t._8, t._9, t._10), t._11, t._12))(
      (w: WorkspaceRequest) => (w.name, w.summary, w.description, w.behavior, w.requestedBy, w.requestDate, w.compliance.phiData, w.compliance.pciData, w.compliance.piiData, w.compliance.id, w.singleUser, w.id)
    )

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

  implicit def decoder(user: User, clock: Clock): Decoder[WorkspaceRequest] = Decoder.instance { json =>
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
    } yield WorkspaceRequest(name, summary, description, behavior, user.distinguishedName, Instant.now(clock), compliance, singleUser, data = data, processing = processing, applications = applications, kafkaTopics = topics)
  }

}
