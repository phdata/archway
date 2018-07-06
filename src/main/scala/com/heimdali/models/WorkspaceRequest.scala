package com.heimdali.models

import java.time.{Clock, Instant}

import cats.data.Kleisli
import com.heimdali.tasks.ProvisionResult
import io.circe._
import io.circe.java8.time._
import io.circe.syntax._

case class WorkspaceRequest(name: String,
                            requestedBy: String,
                            requestDate: Instant,
                            compliance: Compliance,
                            singleUser: Boolean,
                            id: Option[Long] = None,
                            approvals: List[Approval] = List.empty,
                            data: List[HiveDatabase] = List.empty,
                            processing: List[Yarn] = List.empty) {

  def provision() = Kleisli[List, AppConfig, ProvisionResult] =
    Kleisli[List, AppConfig, ProvisionResult] { appConfig =>
      data.map(_.tasks.)
    }
}

object WorkspaceRequest {

  def apply(name: String,
            requestedBy: String,
            requestDate: Instant,
            compliance: Compliance,
            singleUser: Boolean): WorkspaceRequest =
    WorkspaceRequest(name, requestedBy, requestDate, compliance, singleUser, None, List.empty, List.empty, List.empty)

  implicit val encoder: Encoder[WorkspaceRequest] = Encoder.instance { request =>
    request.approvals.foldLeft(
      Json.obj(
        "id" -> request.id.asJson,
        "name" -> request.name.asJson,
        "compliance" -> request.compliance.asJson,
        "data" -> request.data.asJson,
        "processing" -> request.processing.asJson,
        "single_user" -> request.singleUser.asJson,
        "requester" -> request.requestedBy.asJson,
        "requested_date" -> request.requestDate.asJson,
      )
    )((initial, approvals) => initial deepMerge Json.obj("approvals" -> approvals.asJson))
  }

  implicit def decoder(user: User, clock: Clock): Decoder[WorkspaceRequest] = Decoder.instance { json =>
    for {
      name <- json.downField("name").as[String]
      compliance <- json.downField("compliance").as[Compliance]
      singleUser <- json.downField("single_user").as[Boolean]
      data <- json.downField("data").as[List[HiveDatabase]]
      processing <- json.downField("processing").as[List[Yarn]]
    } yield WorkspaceRequest(name, user.username, Instant.now(clock), compliance, singleUser, data = data, processing = processing)
  }

}
