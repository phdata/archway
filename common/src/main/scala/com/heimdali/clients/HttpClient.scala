package com.heimdali.clients

import cats.effect.{Async, Resource}
import cats.implicits._
import com.heimdali.config.ClusterConfig
import com.typesafe.scalalogging.LazyLogging
import io.circe.Decoder
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.headers.Authorization

trait HttpClient[F[_]] {
  def request[A](request: Request[F])(implicit decoder: Decoder[A]): F[A]
}

class CMClient[F[_]: Async](client: Resource[F, Client[F]], clusterConfig: ClusterConfig)
    extends HttpClient[F] with LazyLogging {
  override def request[A](request: Request[F])(implicit decoder: Decoder[A]): F[A] = {
    implicit val entityDecoder: EntityDecoder[F, A] = jsonOf[F, A]
    client.use { ready =>
      for {
        raw <- Async[F].pure(
          request.withHeaders(
            request.headers ++ Seq(
              Authorization(BasicCredentials(clusterConfig.admin.username, clusterConfig.admin.password))
            )
          )
        )
        _ <- Async[F].pure(logger.debug("raw request to CM API: {}", raw))
        response <- ready.fetch[A](raw){resp =>
          if(!resp.status.isSuccess) {
            logger.error("Can't connect to CM {}", resp.body)
          }
          resp.as[A]
        }
      } yield response
    }
  }
}
