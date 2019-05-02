package com.heimdali.startup

import cats.effect._
import com.heimdali.config.ClusterConfig
import com.heimdali.services.LoginContextProvider

import scala.concurrent.duration.FiniteDuration

class SessionMaintainer[F[_] : Sync : ContextShift : Timer](clusterConfig: ClusterConfig,
                                                            loginContextProvider: LoginContextProvider)
  extends ScheduledJob[F] {

  override def stream: fs2.Stream[F, Unit] =
    ScheduledJob.onInterval(
      loginContextProvider.kinit[F],
      clusterConfig.sessionRefresh.asInstanceOf[FiniteDuration]
    )

  def kinit(): F[Unit] =
    loginContextProvider.kinit()

}
