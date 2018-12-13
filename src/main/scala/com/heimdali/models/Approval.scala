package com.heimdali.models

import java.time.{Clock, Instant}

import cats.Show
import cats.implicits._
import doobie.util.{Get, Read}
import io.circe._
import io.circe.syntax._
import io.circe.java8.time._

case class Approval(role: ApproverRole, approver: String, approvalTime: Instant, id: Option[Long] = None)

object Approval {

  implicit val roleGet: Get[ApproverRole] = Get[String].tmap(ApproverRole.parseRole)

  implicit val reader: Read[Approval] =
    Read[(ApproverRole, String, Instant, Option[Long])].map {
      case (role, approver, approvalTime, id) => Approval(role, approver, approvalTime, id)
    }

  implicit def decoder(user: User, clock: Clock): Decoder[Approval] = Decoder.instance( cursor =>
    for {
      role <- cursor.downField("role").as[String]
    } yield Approval(ApproverRole.parseRole(role), user.username, Instant.now(clock))
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