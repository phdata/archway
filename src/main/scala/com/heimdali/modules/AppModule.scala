package com.heimdali.modules

import cats.effect.Effect
import com.heimdali.rest.RestAPI
import com.heimdali.startup.Startup
import java.time.Clock

abstract class AppModule[F[_]](implicit val CF: Effect[F]) {

  implicit val clock: Clock = Clock.systemUTC()

  def startup: Startup[F]

  def restAPI: RestAPI

}
