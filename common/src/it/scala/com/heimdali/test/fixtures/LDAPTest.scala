package com.heimdali.test.fixtures

import com.unboundid.ldap.sdk.{LDAPConnection, LDAPConnectionPool}
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager}

trait LDAPTest {

  val ldapConnectionPool: LDAPConnectionPool = {
    val sslUtil = new SSLUtil(new TrustAllTrustManager)
    val sslSocketFactory = sslUtil.createSSLSocketFactory
    val connection = new LDAPConnection(
      sslSocketFactory,
      appConfig.ldap.adminBinding.server,
      appConfig.ldap.adminBinding.port,
      appConfig.ldap.adminBinding.bindDN,
      appConfig.ldap.adminBinding.bindPassword
    )
    new LDAPConnectionPool(connection, 10)
  }

  val existingUser = "benny"
  val existingPassword = "Jotunn321!"
}