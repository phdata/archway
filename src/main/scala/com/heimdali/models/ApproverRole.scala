package com.heimdali.models

import cats.Show
import doobie.util.meta.Meta

sealed trait ApproverRole

case object Infra extends ApproverRole

case object Risk extends ApproverRole

case object NA extends ApproverRole

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