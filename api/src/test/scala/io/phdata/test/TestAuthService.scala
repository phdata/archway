package io.phdata.test

import cats.data.{EitherT, Kleisli}
import cats.effect.IO
import io.phdata.models.{Token, User}
import io.phdata.test.fixtures.infraApproverUser
import org.http4s.dsl.io._
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedService, Request}
import cats.implicits._
import io.phdata.rest.authentication.{AuthService, LdapAuthService, TokenAuthService}
import io.phdata.models.{Token, User, UserPermissions}
import io.phdata.rest.authentication.{AuthService, TokenAuthService}

class TestAuthService(riskApprover: Boolean = false, platformApprover: Boolean = true)
  extends TokenAuthService[IO] with AuthService[IO] {
  def failure: AuthedService[String, IO] =
    AuthedService[String, IO]({ case req => Forbidden(req.authInfo) })

  val basicValidator: Kleisli[IO, Request[IO], Either[String, Token]] =
    Kleisli[IO, Request[IO], Either[String, Token]](_ => EitherT.right(IO(Token("abc", "abc"))).value)

  val tokenValidator: Kleisli[IO, Request[IO], Either[String, User]] =
    Kleisli[IO, Request[IO], Either[String, User]](_ =>
      Right(infraApproverUser.copy(permissions = UserPermissions(riskApprover, platformApprover))).pure[IO]
    )


  override def clientAuth: AuthMiddleware[IO, Token] = AuthMiddleware(basicValidator, failure)

  override def tokenRoleAuth(auth: User => Boolean): AuthMiddleware[IO, User] = AuthMiddleware(tokenValidator, failure)

  override def tokenAuth: AuthMiddleware[IO, User] = AuthMiddleware(tokenValidator, failure)
}

