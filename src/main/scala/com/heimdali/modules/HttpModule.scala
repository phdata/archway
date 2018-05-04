package com.heimdali.modules

import akka.http.scaladsl.Http
import com.heimdali.clients.{AkkaHttpClient, HttpClient}

trait HttpModule {
  this: ConfigurationModule =>

  val httpExt = Http()

  val http: HttpClient = new AkkaHttpClient(httpExt)

}
