package com.heimdali.ci.provisioning

import org.scalatest.{FlatSpec, Matchers}

class ProvisioningSpec extends FlatSpec with Matchers{

  behavior of "Provisioning"

  it should "be true if numbers are equal" in {
    val result = 1 == 1

    result should be (true)
  }
}
