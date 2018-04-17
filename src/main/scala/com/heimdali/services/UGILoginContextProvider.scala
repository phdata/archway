package com.heimdali.services

import java.security.PrivilegedAction

import javax.security.auth.callback.{Callback, CallbackHandler, NameCallback, PasswordCallback}
import javax.security.auth.login.LoginContext
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.security.UserGroupInformation

import scala.concurrent.{ExecutionContext, Future, Promise}

class UGILoginContextProvider(implicit executionContext: ExecutionContext)
  extends LoginContextProvider with LazyLogging {

  override def elevate[A](user: String)(block: () => A): Future[Option[A]] = {
    logger.warn(s"running block on behalf of $user")
    val promise = Promise[Option[A]]
    val ugi = UserGroupInformation.createProxyUser(user, UserGroupInformation.getLoginUser)
    ugi.doAs(new PrivilegedAction[Option[A]] {
      override def run(): Option[A] = {
        try {
          val result = block()
          promise.success(Some(result))
          Some(result)
        } catch {
          case ex: Throwable =>
            ex.printStackTrace()
            promise.failure(ex)
            None
        }
      }
    })
    promise.future
  }
}
