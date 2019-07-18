package com.heimdali.services

import java.security.{PrivilegedAction, PrivilegedExceptionAction}

import cats._
import cats.effect._
import cats.implicits._
import com.heimdali.config.AppConfig
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.security.UserGroupInformation

import scala.concurrent.ExecutionContext

class UGILoginContextProvider(appConfig: AppConfig) extends LoginContextProvider with LazyLogging {

  override def elevate[F[_]: Async, A](user: String)(block: () => A): F[A] =
    Async[F].async { callback =>
      logger.debug(s"running block on behalf of $user")
      val ugi = UserGroupInformation.createProxyUser(user, UserGroupInformation.getLoginUser)
      ugi.doAs(new PrivilegedExceptionAction[Either[Throwable, A]] {
        override def run(): Either[Throwable, A] = {
          try {
            val result = block()
            callback(Right(result))
            Right(result)
          } catch {
            case ex: Throwable =>
              logger.error(s"Failed to run priveleged block as user '$user'", ex)
              callback(Left(ex))
              Left(ex)
          }
        }
      })
    }

  override def kinit[F[_]: Sync](): F[Unit] =
    Sync[F].delay {
      logger.info("kiniting api service principal ({})", appConfig.rest.principal)
      try {
        UserGroupInformation.loginUserFromKeytab(appConfig.rest.principal, appConfig.rest.keytab)
        ()
      } catch {
        case exc: Throwable =>
          logger.error("Couldn't kinit: {}", exc.toString, exc)
          throw exc
      }
    }

  override def hadoopInteraction[F[_], A](block: F[A])(implicit F: Effect[F]): F[A] =
    Effect[F].delay {
      UserGroupInformation.getLoginUser.doAs(new PrivilegedAction[A] {
        override def run(): A =
          IO.async[A] { cb =>
              F.runAsync(block)(r => IO(cb(r))).unsafeRunSync()
            }
            .unsafeRunSync()
      })
    }
}
