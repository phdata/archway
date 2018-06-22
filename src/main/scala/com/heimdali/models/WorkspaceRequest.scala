package com.heimdali.models

import cats.Show
import cats.syntax.show._
import java.time.Instant
import doobie.util.composite.Composite
import doobie.util.meta.Meta
import io.circe._
import io.circe.syntax._
import io.circe.java8.time._

sealed trait ApproverRole

object ApproverRole {

  def parseRole(role: String): ApproverRole =
    role match {
      case "infra" => Infra
      case "risk" => Risk
    }

  implicit val approverComposite: Meta[ApproverRole] =
    Meta[String].xmap(parseRole, _.toString.toLowerCase)

  implicit def approverShow[A <: ApproverRole]: Show[A] = Show.show(_.getClass.getSimpleName.toLowerCase)

}

case object Infra extends ApproverRole

case object Risk extends ApproverRole

case object NA extends ApproverRole

case class Approval(role: ApproverRole, approver: String, approvalTime: Instant, id: Option[Long] = None)

object Approval {

  implicit val Point2DComposite: Composite[Approval] =
    Composite[(ApproverRole, String, Instant, Option[Long])].imap(
      (t: (ApproverRole, String, Instant, Option[Long])) => Approval(t._1, t._2, t._3, t._4))(
      (p: Approval) => (p.role, p.approver, p.approvalTime, p.id)
    )

  implicit def decoder(user: User): Decoder[Approval] = Decoder.instance( cursor =>
    for {
      role <- cursor.downField("role").as[String]
    } yield Approval(ApproverRole.parseRole(role), user.username, Instant.now())
  )

  implicit val encoder: Encoder[Approval] = Encoder.instance { approval =>
    Json.obj(
      approval.role.toString.toLowerCase -> Json.obj(
        "approver" -> approval.approver.asJson,
        "approval_time" -> approval.approvalTime.asJson
      )
    )
  }

  implicit val approvalShow: Show[Approval] =
    Show.show(a => s"${a.approver} (${a.role.toString}) @ ${a.approvalTime.toString}")

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
