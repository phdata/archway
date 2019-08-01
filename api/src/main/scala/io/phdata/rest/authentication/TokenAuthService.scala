package io.phdata.rest.authentication

import io.phdata.models.User
import org.http4s.server.AuthMiddleware

trait TokenAuthService[F[_]] {
  def tokenAuth: AuthMiddleware[F, User]

  def tokenRoleAuth(auth: User => Boolean): AuthMiddleware[F, User]
}
