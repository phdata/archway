package io.phdata.rest.authentication

import io.phdata.models.Token
import org.http4s.server._

trait AuthService[F[_]] {
  def clientAuth: AuthMiddleware[F, Token]
}
