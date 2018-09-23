package com.heimdali.test.fixtures

import com.typesafe.config.ConfigFactory
import com.unboundid.ldap.sdk.{LDAPConnection, LDAPConnectionPool}
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager}

trait LDAPTest {

  val ldapConnectionPool: LDAPConnectionPool = {
    val sslUtil = new SSLUtil(new TrustAllTrustManager)
    val sslSocketFactory = sslUtil.createSSLSocketFactory
    val connection = new LDAPConnection(
      sslSocketFactory,
      appConfig.ldap.server,
      appConfig.ldap.port,
      appConfig.ldap.bindDN,
      appConfig.ldap.bindPassword
    )
    new LDAPConnectionPool(connection, 10)
  }

  val existingUser = "benny"
  val existingPassword = "Jotunn321!"
}