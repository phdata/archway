package com.heimdali.services

import javax.security.auth.callback.{Callback, CallbackHandler, NameCallback, PasswordCallback}
import javax.security.auth.login.LoginContext

import com.typesafe.config.Config
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.security.UserGroupInformation

import scala.concurrent.ExecutionContext

class UGILoginContextProvider(configuration: Config,
                              hadoopConfiguration: Configuration)
                             (implicit executionContext: ExecutionContext)
  extends LoginContextProvider {

  val username = configuration.getString("admin.username")

  val maybePassword: Option[String] =
    if (configuration.hasPath("admin.password"))
      Some(configuration.getString("admin.password"))
    else
      None

  val maybeKeytab: Option[String] =
    if (configuration.hasPath("admin.keytab"))
      Some(configuration.getString("admin.keytab"))
    else
      None

  class LoginHandler(username: String, password: String) extends CallbackHandler {
    override def handle(callbacks: Array[Callback]): Unit = callbacks.foreach {
      case nameCallback: NameCallback => nameCallback.setName(username)
      case passwordCallback: PasswordCallback => passwordCallback.setPassword(password.toCharArray)
    }
  }

  override def kinit(): Unit = {
    UserGroupInformation.setConfiguration(hadoopConfiguration)
    maybePassword match {
      case Some(password) =>
        val context = new LoginContext("heimdali", new LoginHandler(username, password))
        context.login()
        val subject = context.getSubject
        UserGroupInformation.loginUserFromSubject(subject)
      case _ =>
        UserGroupInformation.loginUserFromKeytab(username, maybeKeytab.get)
    }
  }
}
