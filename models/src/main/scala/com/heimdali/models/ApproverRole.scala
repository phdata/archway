package com.heimdali.models

import cats.Show

sealed trait ApproverRole

case object Infra extends ApproverRole

case object Risk extends ApproverRole

case object Full extends ApproverRole

case object NA extends ApproverRole

object ApproverRole {

  def parseRole(role: String): ApproverRole =
    role match {
      case "infra" => Infra
      case "risk"  => Risk
    }

  implicit def approverShow[A <: ApproverRole]: Show[A] =
    Show.show(_.getClass.getSimpleName.replace("$", "").toLowerCase())

}
