package io.phdata.rest.authentication

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import com.typesafe.scalalogging.LazyLogging
import io.phdata.models.Token
import io.phdata.services.AccountService
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import org.http4s.{BasicCredentials, Request}

class LdapAuthService[F[_]: Sync](accountService: AccountService[F]) extends AuthService[F] with LazyLogging {

  def authStore: Kleisli[OptionT[F, ?], Request[F], Token] =
    Kleisli[OptionT[F, ?], Request[F], Token] { request =>
      request.headers.get(Authorization) match {
        case Some(Authorization(BasicCredentials(username, password))) =>
          accountService.ldapAuth(username, password)
        case _ =>
          OptionT.none
      }
    }

  def clientAuth: AuthMiddleware[F, Token] =
    AuthMiddleware(authStore)

}
