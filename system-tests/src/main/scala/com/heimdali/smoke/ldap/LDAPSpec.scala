package com.heimdali.smoke.ldap

import org.scalatest.{FlatSpec, Matchers}

class LDAPSpec extends FlatSpec with Matchers{

  behavior of "LDAPClientImpl"

  it should "be true if numbers are equal" in {
    val result = 1 == 1

    result should be (true)
  }

}
