package com.heimdali.startup

import cats.effect._
import cats.implicits._
import com.heimdali.config.ClusterConfig
import com.heimdali.services.LoginContextProvider
import com.typesafe.scalalogging.LazyLogging

class SessionMaintainer[F[_] : Sync : Timer : ContextShift](clusterConfig: ClusterConfig,
                                                            loginContextProvider: LoginContextProvider)
  extends ScheduledJob[F] with LazyLogging {

  override def work: F[Unit] =
    for {
      _ <- logger.info("refreshing kerberos ticket").pure[F]
      _ <- loginContextProvider.kinit[F]()
      _ <- logger.info("session refresher going to sleep for {}", clusterConfig.sessionRefresh).pure[F]
      _ <- Timer[F].sleep(clusterConfig.sessionRefresh)
      _ <- work
    } yield ()

}