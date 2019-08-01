package io.phdata.models

import java.time.{Clock, Instant}

import cats.Show
import io.circe._
import io.circe.java8.time._
import io.circe.syntax._

case class Approval(role: ApproverRole, approver: String, approvalTime: Instant, id: Option[Long] = None)

object Approval {

  implicit def decoder(user: User, instant: Instant): Decoder[Approval] =
    Decoder.instance(
      cursor =>
        for {
          role <- cursor.downField("role").as[String]
        } yield Approval(ApproverRole.parseRole(role), user.username, instant)
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
