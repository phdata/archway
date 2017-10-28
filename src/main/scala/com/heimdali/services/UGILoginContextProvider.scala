package com.heimdali.services

import javax.security.auth.Subject
import javax.security.auth.callback.{Callback, CallbackHandler, NameCallback, PasswordCallback}
import javax.security.auth.login.LoginContext

class UGILoginContextProvider extends LoginContextProvider {

  class LoginHandler(username: String, password: String) extends CallbackHandler {
    override def handle(callbacks: Array[Callback]): Unit = callbacks.foreach {
      case nameCallback: NameCallback => nameCallback.setName(username)
      case passwordCallback: PasswordCallback => passwordCallback.setPassword(password.toCharArray)
    }
  }

  def login(username: String, password: String): Subject = {
    val context = new LoginContext(getClass.getSimpleName, new LoginHandler(username, password))
    context.login()
    context.getSubject
  }
}
