package com.heimdali.rest

import cats.data.EitherT
import cats.effect.IO
import cats.syntax.applicative._
import com.heimdali.services.AccountService
import com.heimdali.test.fixtures._
import org.http4s.{Header, Headers, Request, Uri}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class AuthServiceImplSpec extends FlatSpec with Matchers with MockFactory {

  behavior of "Auth Service"

  it should "allow a user to perform approvals" in {
    val accountService = mock[AccountService[IO]]
    accountService.validate _ expects infraApproverToken returning EitherT.right(infraApproverUser.pure[IO])

    val authService = new AuthServiceImpl[IO](accountService)
    val Some(result) = authService.validate(u => u.permissions.platformOperations || u.permissions.riskManagement)(
      Request(uri = Uri.uri("/profile"), headers = Headers(Header("Authorization", infraApproverToken)))).value.unsafeRunSync()
    result.permissions.platformOperations shouldBe true
  }

  it should "not allow a user to perform approvals" in {
    val accountService = mock[AccountService[IO]]
    accountService.validate _ expects basicUserToken returning EitherT.right(basicUser.pure[IO])

    val authService = new AuthServiceImpl[IO](accountService)
    val result = authService.validate(u => u.permissions.platformOperations || u.permissions.riskManagement)(
      Request(uri = Uri.uri("/profile"), headers = Headers(Header("Authorization", basicUserToken)))).value.unsafeRunSync()
    result shouldBe defined
  }

}
