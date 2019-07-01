package com.heimdali.rest.authentication

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import cats.syntax.show._
import com.heimdali.models.User
import com.heimdali.services.AccountService
import com.typesafe.scalalogging.LazyLogging
import org.http4s.Request
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware

class TokenAuthServiceImpl[F[_]: Sync](accountService: AccountService[F]) extends TokenAuthService[F] with LazyLogging {

  def validate(auth: User => Boolean = _ => true): Kleisli[OptionT[F, ?], Request[F], User] =
    Kleisli[OptionT[F, ?], Request[F], User] { request =>
      for {
        header <- OptionT.fromOption(request.headers.get(Authorization.name))
        user <- accountService.validate(header.value.replace("Bearer ", "")).toOption
        _ <- OptionT.some[F](logger.debug(s"authorizing ${user.role.show}"))
        result <- if (auth(user)) OptionT.some(user) else OptionT.none
      } yield result
    }

  override val tokenAuth: AuthMiddleware[F, User] =
    AuthMiddleware.withFallThrough(validate())

  override def tokenRoleAuth(auth: User => Boolean): AuthMiddleware[F, User] =
    AuthMiddleware.withFallThrough(validate(auth))
}
