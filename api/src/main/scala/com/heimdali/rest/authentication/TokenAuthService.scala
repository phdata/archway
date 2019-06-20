package com.heimdali.rest.authentication

import org.http4s.server.AuthMiddleware
import com.heimdali.models.User

trait TokenAuthService[F[_]] {
  def tokenAuth: AuthMiddleware[F, User]

  def tokenRoleAuth(auth: User => Boolean): AuthMiddleware[F, User]
}
