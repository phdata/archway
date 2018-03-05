package com.heimdali.services

import scala.concurrent.Future

trait LoginContextProvider {
  def kinit(): Unit
  def elevate[A](user: String)(block: => A): Future[Option[A]]
}
