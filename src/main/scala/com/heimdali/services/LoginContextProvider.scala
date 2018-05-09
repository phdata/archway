package com.heimdali.services

import scala.concurrent.Future

trait LoginContextProvider {
  def kinit(): Boolean

  def elevate[A](user: String)(block: () => A): Future[Option[A]]
}
