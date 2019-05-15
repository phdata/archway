package com.heimdali.systemtest.smoke.ldap

import org.scalatest.{FlatSpec, Matchers}

class LDAPClientSmokeSpec extends FlatSpec with Matchers{

  behavior of "LDAPClientImpl"

  it should "be true if numbers are equal" in {
    val result = 1 == 1

    result should be (true)
  }

}
