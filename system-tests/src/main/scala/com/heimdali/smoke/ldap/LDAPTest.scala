package com.heimdali.smoke.ldap

import com.heimdali.config.AppConfig
import com.unboundid.ldap.sdk.{FailoverServerSet, LDAPConnectionPool, SimpleBindRequest}
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager}

trait LDAPTest {

  def defaultLDAPAttributes(dn: String, cn: String): List[(String, String)] =
    List(
      "dn" -> dn,
      "objectClass" -> "group",
      "objectClass" -> "top",
      "sAMAccountName" -> cn,
      "cn" -> cn
    )

  def ldapConnectionPool(appConfig: AppConfig): LDAPConnectionPool = {
    val sslUtil = new SSLUtil(new TrustAllTrustManager)
    val sslSocketFactory = sslUtil.createSSLSocketFactory

    val servers: Array[String] = appConfig.ldap.provisioningBinding.server.split(",")
    val ports: Array[Int] = Array.fill(servers.length)(appConfig.ldap.provisioningBinding.port)

    val failoverSet = new FailoverServerSet(
      servers,
      ports,
      sslSocketFactory)

    val bindRequest: SimpleBindRequest =
      new SimpleBindRequest(appConfig.ldap.provisioningBinding.bindDN,
        appConfig.ldap.provisioningBinding.bindPassword)

    new LDAPConnectionPool(failoverSet, bindRequest, 10)
  }

  val existingUser = "benny"
  val existingPassword = "Jotunn321!"
}