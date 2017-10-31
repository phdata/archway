package com.heimdali.services

import java.security.PrivilegedAction
import javax.security.auth.Subject
import javax.security.auth.callback.{Callback, CallbackHandler, NameCallback, PasswordCallback}
import javax.security.auth.login.LoginContext

import org.apache.hadoop.security.UserGroupInformation

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}

class UGILoginContextProvider extends LoginContextProvider {

  class LoginHandler(username: String, password: String) extends CallbackHandler {
    override def handle(callbacks: Array[Callback]): Unit = callbacks.foreach {
      case nameCallback: NameCallback => nameCallback.setName(username)
      case passwordCallback: PasswordCallback => passwordCallback.setPassword(password.toCharArray)
    }
  }

  def login(username: String, password: String): Subject = {
    val context = new LoginContext("heimdali", new LoginHandler(username, password))
    context.login()
    context.getSubject
  }

  override def elevate[A](block: Future[A]): Future[A] = {
    val promise = Promise[A]
    UserGroupInformation.getLoginUser.doAs(new PrivilegedAction[A] {
      override def run() = {
        val result = Await.result(block, 15 seconds)
        promise.success(result)
        result
      }
    })
    promise.future
  }
}
