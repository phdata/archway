package com.heimdali.rest.authentication

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import cats.implicits._
import com.heimdali.models.Token
import com.heimdali.services.AccountService
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.security.SpnegoLoginService
import org.http4s.dsl.io._
import org.http4s.server.AuthMiddleware
import org.http4s.util.CaseInsensitiveString
import org.http4s.{AuthedService, Request, _}

class SpnegoAuthService[F[_]: Sync](accountService: AccountService[F]) extends AuthService[F] with LazyLogging {

  val WWW_AUTH_HEADER = "WWW-Authenticate"
  val AUTHORIZATION_HEADER = "Authorization"

  val NEGOTIATE_HEADER = Header(WWW_AUTH_HEADER, "Negotiate")

  def authStore: Kleisli[F, Request[F], Either[Throwable, Token]] =
    Kleisli[F, Request[F], Either[Throwable, Token]] { request =>
      logger.trace(request.headers.toString())
      request.headers.get(CaseInsensitiveString(AUTHORIZATION_HEADER)) match {
        case Some(h) =>
          accountService.spnegoAuth(h.value)
        case _ =>
          logger.debug(s"$AUTHORIZATION_HEADER header not found, not a valid Spnego request")
          Either.left[Throwable, Token](new Exception("No token provided")).pure[F]
      }
    }

  val onFailure: AuthedService[Throwable, F] = Kleisli(
    _ => OptionT.some(Response(Unauthorized, headers = Headers(NEGOTIATE_HEADER)))
  )

  override def clientAuth = AuthMiddleware(authStore, onFailure)

}
