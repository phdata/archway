package com.heimdali.test

import cats.data.{EitherT, Kleisli}
import cats.effect.IO
import com.heimdali.models.{Token, User}
import com.heimdali.rest.AuthService
import com.heimdali.test.fixtures.infraApproverUser
import org.http4s.dsl.io._
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedService, Request}

class TestAuthService(riskApprover: Boolean = false, platformApprover: Boolean = false)
  extends AuthService[IO] {
  def failure: AuthedService[String, IO] =
    AuthedService[String, IO]({ case req => Forbidden(req.authInfo) })

  val basicValidator: Kleisli[IO, Request[IO], Either[String, Token]] =
    Kleisli[IO, Request[IO], Either[String, Token]](_ => EitherT.right(IO(Token("abc", "abc"))).value)

  val tokenValidator: Kleisli[IO, Request[IO], Either[String, User]] =
    Kleisli[IO, Request[IO], Either[String, User]](_ => EitherT.right(IO(infraApproverUser)).value)

  override def basicAuth: AuthMiddleware[IO, Token] =
    AuthMiddleware(basicValidator, failure)

  override def tokenAuth: AuthMiddleware[IO, User] =
    AuthMiddleware(tokenValidator, failure)

  override def tokenRoleAuth(auth: User => Boolean): AuthMiddleware[IO, User] =
    AuthMiddleware(tokenValidator, failure)
}
