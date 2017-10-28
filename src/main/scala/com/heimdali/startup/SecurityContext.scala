package com.heimdali.startup

import javax.security.auth.Subject

trait SecurityContext {
  def login(username: String, password: String): Subject
}
