package com.heimdali.startup

import cats.effect._
import cats.implicits._
import com.heimdali.config.ClusterConfig
import com.heimdali.services.LoginContextProvider

import scala.concurrent.ExecutionContext

trait SessionMaintainer[F[_]] {
  def setup: F[Unit]
}

class SessionMaintainerImpl[F[_] : Effect](clusterConfig: ClusterConfig,
                            loginContextProvider: LoginContextProvider)
                           (implicit executionContext: ExecutionContext)
  extends SessionMaintainer[F] {

  def keepAlive: IO[Unit] =
    loginContextProvider.kinit() >> Timer[IO].sleep(clusterConfig.sessionRefresh) >> keepAlive

  override def setup: F[Unit] =
    Async[F].liftIO(keepAlive)

}