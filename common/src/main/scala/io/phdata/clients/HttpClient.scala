package io.phdata.clients

import cats.effect.{Async, ConcurrentEffect, Resource}
import cats.implicits._
import io.phdata.config.ClusterConfig
import com.typesafe.scalalogging.LazyLogging
import io.circe.Decoder
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.client.middleware._

trait HttpClient[F[_]] {
  def request[A](request: Request[F])(implicit decoder: Decoder[A]): F[A]
}

class CMClient[F[_]: Async: ConcurrentEffect](client: Resource[F, Client[F]], clusterConfig: ClusterConfig)
    extends HttpClient[F] with LazyLogging {
  override def request[A](request: Request[F])(implicit decoder: Decoder[A]): F[A] = {
    implicit val entityDecoder: EntityDecoder[F, A] = jsonOf[F, A]
    client.use { client =>
      val ready = if (logger.underlying.isTraceEnabled()) {
        val debugLog = Some((message: String) => logger.trace(message).pure[F])
        Logger(true, true, logAction = debugLog)(client)
      } else {
        client
      }

      for {
        raw <- Async[F].pure(
          request.withHeaders(
            request.headers ++
                Headers.of(
                  Authorization(BasicCredentials(clusterConfig.admin.username, clusterConfig.admin.password.value))
                )
          )
        )
        _ <- Async[F].pure(logger.debug("raw request to CM API. Enable trace logging for further information: {}", raw))
        response <- ready.fetch[A](raw)(resp => resp.as[A])
      } yield response
    }
  }
}
