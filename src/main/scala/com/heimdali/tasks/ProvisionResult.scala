package com.heimdali.tasks

import cats.Show
import cats.data.NonEmptyList

sealed trait ProvisionResult { def messages: NonEmptyList[String] }

case class Error(messages: NonEmptyList[String]) extends ProvisionResult

object Error {

  def apply[F](exception: Throwable)(implicit show: Show[F]): Error =
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