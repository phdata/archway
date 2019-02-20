package com.heimdali.modules

import cats.effect.Effect
import com.heimdali.startup._

trait StartupModule[F[_]] {
  this: ExecutionContextModule[F]
    with ConfigurationModule =>

  implicit def effect: Effect[F]

  val sessionMaintainer: SessionMaintainer[F] =
    new SessionMaintainerImpl[F](appConfig.cluster, loginContextProvider)

  val startup: Startup[F] =
    new HeimdaliStartup[F](appConfig.db, appConfig.cluster, sessionMaintainer)

}
