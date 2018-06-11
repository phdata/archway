package com.heimdali.test.fixtures

import cats.data.{EitherT, Kleisli}
import cats.effect.IO
import com.heimdali.models.{Token, User, UserPermissions}
import com.heimdali.rest.AuthService
import org.http4s.dsl.io._
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedService, Request}

class TestAuthService(riskApprover: Boolean = false, platformApprover: Boolean = false) extends AuthService[IO] {
  def failure: AuthedService[String, IO] =
    AuthedService[String, IO]({ case req => Forbidden(req.authInfo) })

  val basicValidator: Kleisli[IO, Request[IO], Either[String, Token]] =
    Kleisli[IO, Request[IO], Either[String, Token]](_ => EitherT.right(IO(Token("abc", "abc"))).value)

  val tokenValidator: Kleisli[IO, Request[IO], Either[String, User]] =
    Kleisli[IO, Request[IO], Either[String, User]](_ => EitherT.right(IO(User("John Doe", standardUsername, UserPermissions(riskApprover, platformApprover)))).value)

  override def basicAuth: AuthMiddleware[IO, Token] =
    AuthMiddleware(basicValidator, failure)

  override def tokenAuth: AuthMiddleware[IO, User] =
    AuthMiddleware(tokenValidator, failure)
}
