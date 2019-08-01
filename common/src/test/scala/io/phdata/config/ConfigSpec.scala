package io.phdata.config

import org.scalatest.{Matchers, PropSpec}

class ConfigSpec extends PropSpec with Matchers {

  property("A config with only one approval should only require 1 approval") {
    val config = ApprovalConfig(Seq.empty, Some(""), None)
    config.required shouldBe 1
  }

}
