package com.heimdali.rest.authentication

import com.heimdali.models.Token
import org.http4s.server._

trait AuthService[F[_]] {
  def clientAuth: AuthMiddleware[F, Token]
}
