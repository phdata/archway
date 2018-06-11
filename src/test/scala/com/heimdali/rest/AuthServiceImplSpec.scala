package com.heimdali.rest

import cats.data.EitherT
import cats.effect.IO
import com.heimdali.services.AccountService
import com.heimdali.test.fixtures.TestAuthService
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import com.heimdali.test.fixtures._
import cats.syntax.applicative._

class AuthServiceImplSpec extends FlatSpec with Matchers with MockFactory {

  behavior of "Auth Service"

  it should "allow a user to perform approvals" in {
    val accountService = mock[AccountService[IO]]
    accountService.validate _ expects infraApproverToken returning EitherT(Right(infraApproverUser).pure[IO])
    val authService = new AuthServiceImpl[IO]()
  }

}
