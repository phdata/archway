package com.heimdali.services

import javax.security.auth.Subject

trait LoginContextProvider {
  def login(username: String, password: String): Subject
}
