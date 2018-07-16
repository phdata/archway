package com.heimdali.tasks

import cats._
import cats.data._

sealed trait ProvisionResult { def messages: NonEmptyList[String] }

object ProvisionResult {

  implicit def resultSemigroup: Monoid[ProvisionResult] = new Monoid[ProvisionResult] {
    def combine(result1: ProvisionResult, result2: ProvisionResult): ProvisionResult =
      (result1, result2) match {
        case (Success(messages1), Success(messages2)) => Success(messages1.concatNel(messages2))
        case (Unknown, Unknown) => Unknown
        case (Unknown, out) => out
        case (out, Unknown) => out
        case (out1, out2) => Error(out1.messages.concatNel(out2.messages))
      }
    def empty: ProvisionResult = Unknown
  }

}

case object Unknown extends ProvisionResult {
  override def messages: NonEmptyList[String] = NonEmptyList.one(s"Undetermined provisioning result")
}

case class Error(messages: NonEmptyList[String]) extends ProvisionResult

object Error {

  def apply[A](exception: Throwable)(implicit show: Show[A]): Error =
    apply(NonEmptyList.of(
      s"""$show FAILED due to ${exception.getMessage}"""
    ))

}

case class Success(messages: NonEmptyList[String]) extends ProvisionResult

object Success {

  def apply[A](message: String)(implicit show: Show[A]): Success =
    apply(NonEmptyList.of(
      s"$show SUCCEEDED",
      s"$show: message"
    ))


  def apply[A](implicit show: Show[A]): Success =
    apply(NonEmptyList.one(
      s"$show SUCCEEDED"
    ))

}
