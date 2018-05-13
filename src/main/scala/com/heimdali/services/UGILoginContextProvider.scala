package com.heimdali.services

import java.security.PrivilegedAction

import com.typesafe.scalalogging.LazyLogging
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

  override def kinit(): Boolean = {
    logger.info("kiniting api service principal")
    try {
      UserGroupInformation.loginUserFromKeytab(sys.env("KEYTAB_FILE"), sys.env("HEIMDALI_API_SERVICE_PRINCIPAL"))
      true
    } catch {
      case exc: Throwable =>
        logger.error("Couldn't kinit: {}", exc.toString, exc)
        false
    }
  }
}
