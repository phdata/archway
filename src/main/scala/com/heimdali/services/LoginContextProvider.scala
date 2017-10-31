package com.heimdali.services

import javax.security.auth.Subject

import scala.concurrent.Future

trait LoginContextProvider {
  def login(username: String, password: String): Subject
  def elevate[A](block: Future[A]): Future[A]
}
