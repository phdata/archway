package com.heimdali.modules

import cats.effect._
import com.heimdali.rest.RestAPI
import com.heimdali.startup.Startup

abstract class IOAppModule[F[_]](implicit val contextShift: ContextShift[IO],
                           val timer: Timer[IO],
                           val effect: ConcurrentEffect[F])
  extends ExecutionContextModule[IO] {

  def startup: Startup[IO]

  def restAPI: RestAPI

}
