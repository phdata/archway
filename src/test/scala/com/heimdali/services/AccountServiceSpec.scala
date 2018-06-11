package com.heimdali.services

import cats.effect.IO
import com.heimdali.clients.LDAPClient
import com.heimdali.config.{ApprovalConfig, RestConfig}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class AccountServiceSpec extends FlatSpec with MockFactory with Matchers {

  behavior of "Account Service"

  it should "return appropriate roles" in {
    val approvalConfig = ApprovalConfig("cn=foo,dc=jotunn,dc=io", "cn=bar,dc=jotunn,dc=io")
    val restConfig = RestConfig(1234, "abc")

    val accountService = new AccountServiceImpl[IO](mock[LDAPClient[IO]], restConfig, approvalConfig)

    val Right(user) = accountService.validate().value.unsafeRunSync()
    /**
      * {
          "name": "Dude Doe",
          "username": "username",
          "permissions": {
            "risk_management": false,
            "platform_operations": true
          }
      * }
      */

    user.permissions.platformOperations should be(true)
  }

}
