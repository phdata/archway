package com.heimdali.provisioning

import cats._
import cats.data._

sealed trait ProvisionResult

case object Unknown extends ProvisionResult {
  def message(workspaceId: Long): NonEmptyList[Message] =
    NonEmptyList.one(SimpleMessage(workspaceId, s"Undetermined provisioning result"))
}

case object NoOp extends ProvisionResult {
  def message(workspaceId: Long, provisionType: String): NonEmptyList[Message] =
    NonEmptyList.one(SimpleMessage(workspaceId, s"Nothing to do for $provisionType"))
}

case object Error extends ProvisionResult {
  def message[A](resource: A, workspaceId: Long, exception: Exception)(implicit show: Show[A]): NonEmptyList[Message] =
    NonEmptyList.one(
      ExceptionMessage(workspaceId, s"""${show.show(resource)} for workspace $workspaceId FAILED due to ${exception.getMessage}""", exception)
    )
}

case object Success extends ProvisionResult {
  def message[A](resource: A, workspaceId: Long)(implicit show: Show[A]): NonEmptyList[Message] =
    NonEmptyList.of(
      SimpleMessage(workspaceId, s"${show.show(resource)} for workspace $workspaceId SUCCEEDED"))
}

object ProvisionResult {

  implicit def resultSemigroup: Monoid[ProvisionResult] = new Monoid[ProvisionResult] {
    def combine(result1: ProvisionResult, result2: ProvisionResult): ProvisionResult =
      (result1, result2) match {
        case (Success, Success) => Success // Success + Success = Success
        case (_@(NoOp | Unknown), Unknown) => Unknown // NoOp and Unknown are passive, so just keep using Unknown
        case (_@(NoOp | Unknown), out) => out // NoOp and Unknown are passive, so just use whatever new result we have
        case (out, _@(NoOp | Unknown)) => out // NoOp and Unknown are passive, so just keep what we started with
        case (_, _) => Error // There has been an error or is a new error, so we have an Error
      }

    def empty: ProvisionResult = Unknown
  }

}
