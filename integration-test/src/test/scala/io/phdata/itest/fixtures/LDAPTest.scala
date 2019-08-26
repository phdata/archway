package io.phdata.itest.fixtures

import cats.effect.IO
import io.phdata.clients.LDAPClientImpl
import com.unboundid.ldap.sdk.{FailoverServerSet, LDAPConnectionPool, SimpleBindRequest}
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager}

trait LDAPTest {
  val lookupClient = new LDAPClientImpl[IO](itestConfig.ldap, _.lookupBinding)
  val provisioningClient = new LDAPClientImpl[IO](itestConfig.ldap, _.provisioningBinding)

  val ldapConnectionPool: LDAPConnectionPool = {
    val sslUtil = new SSLUtil(new TrustAllTrustManager)
    val sslSocketFactory = sslUtil.createSSLSocketFactory

    val servers: Array[String] = itestConfig.ldap.lookupBinding.server.split(",")
    val ports: Array[Int] = Array.fill(servers.length)(itestConfig.ldap.provisioningBinding.port)

    val failoverSet = new FailoverServerSet(
      servers,
      ports,
      sslSocketFactory)

    val bindRequest: SimpleBindRequest =
      new SimpleBindRequest(itestConfig.ldap.lookupBinding.bindDN,
        itestConfig.ldap.lookupBinding.bindPassword.value)

    new LDAPConnectionPool(failoverSet, bindRequest, 10)
  }
}

