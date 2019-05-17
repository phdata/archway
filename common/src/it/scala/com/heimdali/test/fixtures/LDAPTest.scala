package com.heimdali.test.fixtures

import com.unboundid.ldap.sdk.{
  FailoverServerSet,
  LDAPConnectionPool,
  SimpleBindRequest
}
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager}

trait LDAPTest {

  val ldapConnectionPool: LDAPConnectionPool = {
    val sslUtil = new SSLUtil(new TrustAllTrustManager)
    val sslSocketFactory = sslUtil.createSSLSocketFactory

    val servers: Array[String] = appConfig.ldap.lookupBinding.server.split(",")
    val ports: Array[Int] = Array.fill(servers.length)(appConfig.ldap.provisioningBinding.port)

    val failoverSet = new FailoverServerSet(
      servers,
      ports,
      sslSocketFactory)

    val bindRequest: SimpleBindRequest =
      new SimpleBindRequest(appConfig.ldap.lookupBinding.bindDN,
                            appConfig.ldap.lookupBinding.bindPassword)

    new LDAPConnectionPool(failoverSet, bindRequest, 10)
  }

  val existingUser = "benny"
  val existingPassword = "Jotunn321!"
}
