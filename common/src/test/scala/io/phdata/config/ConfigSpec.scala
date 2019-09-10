package io.phdata.config

import org.scalatest.{Matchers, PropSpec}

class ConfigSpec extends PropSpec with Matchers {

  property("A config with only one approval should only require 1 approval") {
    val config = ApprovalConfig(Seq.empty, Some(""), None)
    config.required shouldBe 1
  }

  property("Password toString method should mask real password") {
    val password = Password("secret")
    password.toString shouldBe "***********"
  }

  property("Password value should return real password") {
    val password = Password("secret")
    password.value shouldBe "secret"
  }

}
