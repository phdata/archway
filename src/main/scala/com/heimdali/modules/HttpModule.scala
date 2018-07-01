package com.heimdali.modules

import com.heimdali.clients.{CMClient, HttpClient}
import org.http4s.client.Client
import org.http4s.client.blaze.{BlazeClientConfig, Http1Client}
import scala.concurrent.duration._

trait HttpModule[F[_]] {
  this: AppModule[F] with ConfigurationModule =>

  private val client: F[Client[F]] = Http1Client[F](
    BlazeClientConfig.defaultConfig
      .copy(responseHeaderTimeout = 60 seconds)
  )

  val http: HttpClient[F] = new CMClient[F](client, appConfig.cluster)

}
