package com.heimdali.provisioning

import cats._
import cats.data._

sealed trait ProvisionResult {
  def messages: NonEmptyList[Message] }

object ProvisionResult {

  implicit def resultSemigroup: Monoid[ProvisionResult] = new Monoid[ProvisionResult] {
    def combine(result1: ProvisionResult, result2: ProvisionResult): ProvisionResult =
      (result1, result2) match {
        case (Success(messages1), Success(messages2)) => Success(messages1.concatNel(messages2))
        case (_ @ (NoOp(_) | Unknown), Unknown) => Unknown
        case (_ @ (NoOp(_) | Unknown), out) => out
        case (out, _ @ (NoOp(_) | Unknown)) => out
        case (out1, out2) => Error(out1.messages.concatNel(out2.messages))
      }
    def empty: ProvisionResult = Unknown
  }

}

case object Unknown extends ProvisionResult {
  override def messages: NonEmptyList[Message] = NonEmptyList.one(SimpleMessage(None, s"Undetermined provisioning result"))
}

case class NoOp(provisionType: String) extends ProvisionResult {
  override def messages: NonEmptyList[Message] = NonEmptyList.one(SimpleMessage(None, s"Nothing to do for $provisionType"))
}

case class Error(messages: NonEmptyList[Message]) extends ProvisionResult

object Error {

  def apply[A](workspaceId: Option[Long], a: A, exception: Throwable)(implicit show: Show[A]): Error = {
    exception.printStackTrace()
    val workspaceString = workspaceId.map(_.toString).getOrElse("unknown")
    val message = s"""${show.show(a)} for workspace $workspaceString FAILED due to ${exception.getMessage}"""
    apply(NonEmptyList.of(
      ExceptionMessage(workspaceId, message, exception)
    ))
  }

  def apply[A](workspaceId: Long, a: A, exception: Throwable)(implicit show: Show[A]): Error = {
    apply(Some(workspaceId), a, exception)
  }

}

case class Success(messages: NonEmptyList[Message]) extends ProvisionResult

object Success {

  def apply[A](workspaceId: Option[Long], a: A, message: String)(implicit show: Show[A]): Success = {
    val workspaceString = workspaceId.map(_.toString).getOrElse("unknown")

    apply(NonEmptyList.of(
      SimpleMessage(workspaceId, s"${show.show(a)} for workspace $workspaceString SUCCEEDED, $message")))
  }

  def apply[A](workspaceId: Option[Long], a: A)(implicit show: Show[A]): Success = {
    val workspaceString = workspaceId.map(_.toString).getOrElse("unknown")

    apply(NonEmptyList.one(
      SimpleMessage(workspaceId: Option[Long], s"${show.show(a)} for workspace $workspaceString SUCCEEDED")
    ))
  }

}
