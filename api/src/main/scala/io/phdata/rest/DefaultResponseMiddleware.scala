package io.phdata.rest

import cats.Functor
import org.http4s.{Header, HttpRoutes, Response}

class DefaultResponseMiddleware[F[_]: Functor] {

  def addHeader(resp: Response[F]) =
    resp.putHeaders(Header("X-Frame-Options", "deny"))

  def apply(service: HttpRoutes[F]): HttpRoutes[F] =
    service.map(addHeader(_))
}
