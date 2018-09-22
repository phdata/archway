package com.heimdali.services

import java.security.{PrivilegedAction, PrivilegedExceptionAction}

import cats._
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.security.UserGroupInformation

import scala.concurrent.ExecutionContext

class UGILoginContextProvider(implicit val executionContext: ExecutionContext)
  extends LoginContextProvider with LazyLogging {

  override def elevate[F[_] : Async, A](user: String)(block: () => A): F[A] =
    Async[F].async { callback =>
      logger.warn(s"running block on behalf of $user")
      val ugi = UserGroupInformation.createProxyUser(user, UserGroupInformation.getLoginUser)
      ugi.doAs(new PrivilegedExceptionAction[Either[Throwable, A]] {
        override def run(): Either[Throwable, A] = {
          try {
            val result = block()
            callback(Right(result))
            Right(result)
          } catch {
            case ex: Throwable =>
              ex.printStackTrace()
              callback(Left(ex))
              Left(ex)
          }
        }
      })
    }

  override def kinit(): IO[Unit] =
    IO {
      logger.info("kiniting api service principal")
      try {
        UserGroupInformation.loginUserFromKeytab(sys.env("HEIMDALI_API_SERVICE_PRINCIPAL"), s"${sys.env("PWD")}/heimdali.keytab")
        ()
      } catch {
        case exc: Throwable =>
          logger.error("Couldn't kinit: {}", exc.toString, exc)
          throw exc
      }
    }

  override def hadoopInteraction[F[_], A](block: F[A])(implicit F: Effect[F]): F[A] =
    Effect[F].delay{
      UserGroupInformation.getLoginUser.doAs(new PrivilegedAction[A] {
        override def run(): A =
          IO.async[A] { cb =>
            F.runAsync(block)(r => IO(cb(r))).unsafeRunSync()
          }.unsafeRunSync()
      })
    }
}
