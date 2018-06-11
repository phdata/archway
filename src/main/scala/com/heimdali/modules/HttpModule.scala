package com.heimdali.modules

import com.heimdali.clients.{CMClient, HttpClient}
import org.http4s.client.Client
import org.http4s.client.blaze.Http1Client

trait HttpModule[F[_]] {
  this: AppModule[F]
    with ConfigurationModule =>

  private val client: F[Client[F]] = Http1Client[F]()

  val http: HttpClient[F] = new CMClient[F](client, appConfig.cluster)

}
