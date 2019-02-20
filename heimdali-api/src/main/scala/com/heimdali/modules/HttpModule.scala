package com.heimdali.modules

import cats.effect.{Async, ConcurrentEffect, Resource}
import com.heimdali.clients.{CMClient, HttpClient}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import scala.concurrent.duration._

trait HttpModule[F[_]] {
  this: ConfigurationModule
    with ExecutionContextModule[F] =>

  implicit def effect: ConcurrentEffect[F]

  private val client: Resource[F, Client[F]] =
    BlazeClientBuilder[F](executionContext)
      .withRequestTimeout(5 minutes)
      .withResponseHeaderTimeout(5 minutes)
      .resource

  val http: HttpClient[F] = new CMClient[F](client, appConfig.cluster)

}
