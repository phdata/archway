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
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.authentication.BasicAuth
import org.http4s.server.middleware.authentication.BasicAuth.BasicAuthenticator
import org.http4s.circe._
import org.http4s.{AuthedService, BasicCredentials, Request}

trait AuthService[F[_]] {
  def basicAuth: AuthMiddleware[F, Token]

  def tokenAuth: AuthMiddleware[F, User]
}

class AuthServiceImpl[F[_] : Sync](accountService: AccountService[F])
  extends AuthService[F] with LazyLogging {

  object dsl extends Http4sDsl[F]

  import dsl._

  def failure(reason: String): Json = Json.obj(
    "message" -> reason.asJson
  )

  def authStore(accountService: AccountService[F]): BasicAuthenticator[F, Token] =
    (creds: BasicCredentials) =>
      accountService.login(creds.username, creds.password).value

  def basicAuth: AuthMiddleware[F, Token] =
    BasicAuth("heimdali", authStore(accountService))

  val onFailure: AuthedService[Json, F] =
    Kleisli({
      req =>
        OptionT.liftF(Forbidden(req.authInfo))
    })

  val validate: Kleisli[F, Request[F], Either[Json, User]] =
    Kleisli[F, Request[F], Either[Json, User]] { request =>
      val lookup: EitherT[F, Json, User] = for {
        header <- EitherT.fromEither[F](request.headers.get(Authorization.name).toRight(failure("Missing authorization header")))
        user <- accountService.validate(header.value).leftMap(e => failure(e.getMessage))
      } yield user
      lookup.value
    }

  override val tokenAuth: AuthMiddleware[F, User] = AuthMiddleware(validate, onFailure)

}