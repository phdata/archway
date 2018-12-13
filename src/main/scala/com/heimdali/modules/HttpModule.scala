package com.heimdali.modules

import cats.effect.{Async, ConcurrentEffect, Resource}
import com.heimdali.clients.{CMClient, HttpClient}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

trait HttpModule[F[_]] {
  this: ConfigurationModule
    with ExecutionContextModule[F] =>

  implicit def effect: ConcurrentEffect[F]

  private val client: Resource[F, Client[F]] =
    BlazeClientBuilder[F](executionContext).resource

  val http: HttpClient[F] = new CMClient[F](client, appConfig.cluster)

}
