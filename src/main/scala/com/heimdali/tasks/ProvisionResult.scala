package com.heimdali.tasks

import cats._
import cats.data._

sealed trait ProvisionResult { def messages: NonEmptyList[String] }

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
  override def messages: NonEmptyList[String] = NonEmptyList.one(s"Undetermined provisioning result")
}

case class NoOp(provisionType: String) extends ProvisionResult {
  override def messages: NonEmptyList[String] = NonEmptyList.one(s"Nothing to do for $provisionType")
}

case class Error(messages: NonEmptyList[String]) extends ProvisionResult

object Error {

  def apply[A](a: A, exception: Throwable)(implicit show: Show[A]): Error = {
    exception.printStackTrace()
    apply(NonEmptyList.of(
      s"""${show.show(a)} FAILED due to ${exception.getMessage}"""
    ))
  }

}

case class Success(messages: NonEmptyList[String]) extends ProvisionResult

object Success {

  def apply[A](a: A, message: String)(implicit show: Show[A]): Success =
    apply(NonEmptyList.of(
      s"${show.show(a)} SUCCEEDED",
      s"${show.show(a)}: $message"
    ))


  def apply[A](a: A)(implicit show: Show[A]): Success =
    apply(NonEmptyList.one(
      s"${show.show(a)} SUCCEEDED"
    ))

}
