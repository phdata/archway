package com.heimdali.services

import java.net.URL

import cats.effect.IO

import com.heimdali.clients.KerberosClientImpl
import com.heimdali.itest.fixtures.KerberosTest
import com.kerb4j.client.SpnegoClient
import com.heimdali.itest.fixtures.itestConfig
import org.scalatest.FlatSpec


class KerberosClientIntegrationSpec
  extends FlatSpec
    with KerberosTest {

  it should "auth a user using Spnego " in new Context {
    // System.setProperty("sun.security.krb5.debug", "true")
    val spnegoContext = SpnegoClient.loginWithKeyTab(itestConfig.rest.principal, itestConfig.rest.keytab)

    val spnegoHeader = spnegoContext.createAuthroizationHeader(
      new URL("https://edge1.valhalla.phdata.io"))

    val result = for {username <- kerberosClient.spnegoUsername(spnegoHeader)} yield username

    assert(result.value.unsafeRunSync() == Right("pulse/edge1.valhalla.phdata.io"))
  }

  trait Context {
    // TODO switch to the heimdali api keytab when it has an http principal
    val modifiedRestConfig = itestConfig.rest.copy(httpPrincipal = "HTTP/edge1.valhalla.phdata.io@PHDATA.IO").copy(keytab = "itest-config/pulse.keytab")

    val kerberosClient = new KerberosClientImpl[IO](itestConfig.copy(rest = modifiedRestConfig))
  }

}
