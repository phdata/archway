package com.heimdali.models

import java.time.{Clock, Instant}

import cats.Show
import cats.implicits._
import doobie.util.composite.Composite
import io.circe._
import io.circe.syntax._
import io.circe.java8.time._

case class Approval(role: ApproverRole, approver: String, approvalTime: Instant, id: Option[Long] = None)

object Approval {

  implicit val Point2DComposite: Composite[Approval] =
    Composite[(ApproverRole, String, Instant, Option[Long])].imap(
      (t: (ApproverRole, String, Instant, Option[Long])) => Approval(t._1, t._2, t._3, t._4))(
      (p: Approval) => (p.role, p.approver, p.approvalTime, p.id)
    )

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