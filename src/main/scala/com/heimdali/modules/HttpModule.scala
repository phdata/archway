package com.heimdali.modules

import akka.http.scaladsl.{Http, HttpExt}

trait HttpModule {
  this: AkkaModule =>

  val http: HttpExt = Http()

}
