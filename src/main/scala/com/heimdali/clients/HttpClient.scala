package com.heimdali.clients

import cats.effect.{Async, Effect, Sync}
import com.heimdali.config.ClusterConfig
import io.circe.Decoder
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.headers.Authorization
import cats.implicits._

trait HttpClient[F[_]] {
  def request[A](request: Request[F])
                (implicit decoder: Decoder[A]): F[A]
}

class CMClient[F[_] : Async](client: F[Client[F]],
                             clusterConfig: ClusterConfig)
  extends HttpClient[F] {
  override def request[A](request: Request[F])
                         (implicit decoder: Decoder[A]): F[A] = {
    implicit val entityDecoder: EntityDecoder[F, A] = jsonOf[F, A]
    for {
      ready <- client
      raw <- Async[F].pure(request.withHeaders(Headers(Authorization(BasicCredentials(clusterConfig.admin.username, clusterConfig.admin.password)))))
      response <- ready.expect[A](raw)
    } yield response
  }
}