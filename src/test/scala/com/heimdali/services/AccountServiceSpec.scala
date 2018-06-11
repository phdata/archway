package com.heimdali.services

import cats.effect.IO
import com.heimdali.clients.LDAPClient
import com.heimdali.config.{ApprovalConfig, RestConfig}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class AccountServiceSpec extends FlatSpec with MockFactory with Matchers {

  "Account Service"

  it should "return appropriate roles" in {
    val approvalConfig = ApprovalConfig("cn=foo,dc=jotunn,dc=io", "cn=bar,dc=jotunn,dc=io")
    val restConfig = RestConfig(1234, "abc")

    val accountService = new AccountServiceImpl[IO](mock[LDAPClient[IO]], restConfig, approvalConfig)

    val user = accountService.validate("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJuYW1lIjoiRHVkZSBEb2UiLCJ1c2VybmFtZSI6InVzZXJuYW1lIiwicGVybWlzc2lvbnMiOnsicmlza19tYW5hZ2VtZW50IjpmYWxzZSwicGxhdGZvcm1fb3BlcmF0aW9ucyI6dHJ1ZX19.ozdnHUoKPseTvh4Cjymc7YnbKWzTuIDVYCLqHlyy25lg0BKPsUK786Q_JmvNnmmZ5dhr0twU1IXUfpfsXdWm7Q")
    user.value.unsafeRunSync().isRight
  }

}
