package com.heimdali.rest

import cats.data._
import cats.effect._
import com.heimdali.models.{Token, User}
import com.heimdali.services.AccountService
import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax._
import io.circe.Json
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.server._
import org.http4s.server.middleware.authentication.BasicAuth
import org.http4s.server.middleware.authentication.BasicAuth.BasicAuthenticator
import org.http4s.circe._
import org.http4s.{AuthedService, BasicCredentials, Request}
import cats.syntax.applicative._

trait AuthService[F[_]] {
  def basicAuth: AuthMiddleware[F, Token]

  def tokenAuth: AuthMiddleware[F, User]

  def tokenRoleAuth(auth: User => Boolean): AuthMiddleware[F, User]
}

class AuthServiceImpl[F[_] : Sync](accountService: AccountService[F])
  extends AuthService[F] with LazyLogging {

  object dsl extends Http4sDsl[F]

  import dsl._

  def failure(reason: String): Json = Json.obj(
    "message" -> reason.asJson
  )

  def authStore: Kleisli[OptionT[F, ?], Request[F], Token] =
    Kleisli[OptionT[F, ?], Request[F], Token] { request =>
      request.headers.get(Authorization) match {
        case Some(Authorization(BasicCredentials(creds))) =>
          accountService.login(creds.username, creds.password)
        case _ =>
         OptionT.none
      }
    }

  def basicAuth: AuthMiddleware[F, Token] =
    AuthMiddleware.withFallThrough(authStore)

  val onFailure: AuthedService[Json, F] =
    Kleisli({
      req =>
        OptionT.liftF(Forbidden(req.authInfo))
    })

  def validate(auth: User => Boolean = _ => true): Kleisli[F, Request[F], Either[Json, User]] =
    Kleisli[F, Request[F], Either[Json, User]] { request =>
      (for {
        header <- EitherT.fromEither[F](request.headers.get(Authorization.name).toRight(failure("Missing authorization header")))
        user <- accountService.validate(header.value).leftMap(e => failure(e.getMessage))
        result <- EitherT.fromEither[F](if(auth(user)) Right(user) else Left(failure("Not allowed")))
      } yield result).value
    }

  override val tokenAuth: AuthMiddleware[F, User] =
    AuthMiddleware(validate(), onFailure)

  override def tokenRoleAuth(auth: User => Boolean): AuthMiddleware[F, User] =
    AuthMiddleware(validate(auth), onFailure)
}
