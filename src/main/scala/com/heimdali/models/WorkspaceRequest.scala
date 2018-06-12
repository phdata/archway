package com.heimdali.models

import java.time.Instant

import cats.Show
import io.circe._
import io.circe.syntax._
import io.circe.java8.time._

sealed trait ApproverRole

object ApproverRole {

  implicit def approverShow[A <: ApproverRole]: Show[A] = Show.show(_.getClass.getSimpleName.toLowerCase)

}

case object Infra extends ApproverRole

case object Risk extends ApproverRole

case object NA extends ApproverRole

case class Approval(role: ApproverRole, approver: String, approvalTime: Instant, id: Option[Long] = None)

object Approval {

  implicit val encoder: Encoder[Approval] = Encoder.instance { approval =>
    Json.obj(
      approval.role.toString.toLowerCase -> Json.obj(
        "approver" -> approval.approver.asJson,
        "approval_time" -> approval.approvalTime.asJson
      )
    )
  }

}

case class WorkspaceRequest(name: String,
                            requestedBy: String,
                            requestDate: Instant,
                            compliance: Compliance,
                            singleUser: Boolean,
                            id: Option[Long] = None,
                            approvals: List[Approval] = List.empty,
                            data: List[HiveDatabase] = List.empty,
                            processing: List[Yarn] = List.empty)

object WorkspaceRequest {

  def apply(name: String,
            requestedBy: String,
            requestDate: Instant,
            compliance: Compliance,
            singleUser: Boolean): WorkspaceRequest =
    WorkspaceRequest(name, requestedBy, requestDate, compliance, singleUser, None, List.empty, List.empty, List.empty)

  implicit val encoder: Encoder[WorkspaceRequest] = Encoder.instance { request =>
//    request.approvals.foldLeft(
      Json.obj(
        "id" -> request.id.asJson,
        "name" -> request.name.asJson,
        "compliance" -> request.compliance.asJson,
        "data" -> request.data.asJson,
        "processing" -> request.processing.asJson,
        "single_user" -> request.singleUser.asJson
      )
//    )(_ deepMerge _.asJson)
  }

  implicit def decoder(implicit user: User): Decoder[WorkspaceRequest] = Decoder.instance { json =>
    for {
      name <- json.downField("name").as[String]
      compliance <- json.downField("compliance").as[Compliance]
      singleUser <- json.downField("single_user").as[Boolean]
      data <- json.downField("data").as[List[HiveDatabase]]
      processing <- json.downField("processing").as[List[Yarn]]
    } yield WorkspaceRequest(name, user.username, Instant.now(), compliance, singleUser, data = data, processing = processing)
  }

}