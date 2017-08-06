package com.heimdali.controller

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import org.scalatest.{FlatSpec, Matchers}

class CredentialsSpec extends FlatSpec with Matchers {

  behavior of "CredentialsSpec"

  it should "unapply" in {
    val encoded = BaseEncoding.base64().encode("username:password".getBytes(Charsets.UTF_8))
    val Credentials(username, password) = s"Basic $encoded"

    username should be ("username")
    password should be ("password")
  }

}
