package com.heimdali.test.fixtures

import com.typesafe.config.ConfigFactory
import com.unboundid.ldap.sdk.LDAPConnection
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager}

trait LDAPTest {
  val config = ConfigFactory.load()
  val sslUtil = new SSLUtil(new TrustAllTrustManager)
  val sslSocketFactory = sslUtil.createSSLSocketFactory
  val connection = new LDAPConnection(
    sslSocketFactory,
    config.getString("ldap.server"),
    config.getInt("ldap.port"),
    config.getString("ldap.bindDN"),
    config.getString("ldap.bindPassword")
  )

  val existingUser = "benny"
  val existingPassword = "Jotunn321!"
}