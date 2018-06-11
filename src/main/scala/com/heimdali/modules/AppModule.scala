package com.heimdali.modules

import cats.effect.Effect
import com.heimdali.rest.RestAPI
import com.heimdali.startup.Startup

abstract class AppModule[F[_]](implicit val CF: Effect[F]) {

  def startup: Startup[F]

  def restAPI: RestAPI

}
