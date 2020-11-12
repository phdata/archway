package io.phdata.clients

import io.circe.Decoder
import org.http4s._

trait HttpClient[F[_]] {
  def request[A](request: Request[F])(implicit decoder: Decoder[A]): F[A]
}
