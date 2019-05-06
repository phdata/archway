package com.heimdali.startup

import cats.effect._
import cats.implicits._
import com.heimdali.config.ClusterConfig
import com.heimdali.services.LoginContextProvider

import scala.concurrent.duration.FiniteDuration

class SessionMaintainer[F[_] : Sync](clusterConfig: ClusterConfig,
                                     loginContextProvider: LoginContextProvider)
                                    (implicit timer: Timer[F])
  extends ScheduledJob[F] {

  override def start: F[Unit] =
    loginContextProvider.kinit().flatMap { _ =>
      ScheduledJob.onInterval(
        loginContextProvider.kinit[F],
        clusterConfig.sessionRefresh.asInstanceOf[FiniteDuration]
      )
    }

}