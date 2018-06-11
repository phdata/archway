package com.heimdali.startup

import cats.effect.{IO, Timer}
import cats.syntax.flatMap._
import com.heimdali.config.ClusterConfig
import com.heimdali.services.LoginContextProvider

import scala.concurrent.ExecutionContext

trait SessionMaintainer {
  def setup: IO[Unit]
}

class SessionMaintainerImpl(clusterConfig: ClusterConfig,
                            loginContextProvider: LoginContextProvider)
                           (implicit executionContext: ExecutionContext)
  extends SessionMaintainer {

  override def setup: IO[Unit] =
    loginContextProvider.kinit() >> Timer[IO].sleep(clusterConfig.sessionRefresh) >> setup

}