package com.heimdali.startup

import cats.effect._
import cats.implicits._
import com.heimdali.config.ClusterConfig
import com.heimdali.services.LoginContextProvider

import scala.concurrent.duration.FiniteDuration

trait SessionMaintainer[F[_]] {

  def setup: F[Unit]

}

class SessionMaintainerImpl[F[_] : Sync](clusterConfig: ClusterConfig,
                            loginContextProvider: LoginContextProvider)
                           (implicit timer: Timer[F])
  extends SessionMaintainer[F] {

  override def setup: F[Unit] =
    loginContextProvider.kinit().flatMap { _ =>
      fs2.Stream.awakeEvery[F](clusterConfig.sessionRefresh.asInstanceOf[FiniteDuration])
        .evalMap(_ => loginContextProvider.kinit())
        .compile
        .drain
    }

}