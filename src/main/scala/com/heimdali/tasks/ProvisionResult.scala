package com.heimdali.tasks

import cats.Show
import cats.data.NonEmptyList

sealed trait ProvisionResult[A] { def messages: NonEmptyList[String] }

case class Error[A](messages: NonEmptyList[String]) extends ProvisionResult[A]

object Error {

  def apply[A](exception: Throwable)(implicit show: Show[A]): Error[A] =
    apply[A](NonEmptyList.of(
      s"""$show FAILED due to ${exception.getMessage}"""
    ))

}

case class Success[A](messages: NonEmptyList[String]) extends ProvisionResult[A]

object Success {

  def apply[A](message: String)(implicit show: Show[A]): Success[A] =
    apply[A](NonEmptyList.of(
      s"$show SUCCEEDED",
      s"$show: message"
    ))


  def apply[A](implicit show: Show[A]): Success[A] =
    apply(NonEmptyList.one(
      s"$show SUCCEEDED"
    ))

}