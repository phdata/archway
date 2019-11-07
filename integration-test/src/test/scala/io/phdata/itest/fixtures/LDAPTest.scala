package io.phdata.itest.fixtures

import cats.effect.IO
import com.unboundid.ldap.sdk.{FailoverServerSet, LDAPConnectionPool, SimpleBindRequest}
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager, TrustStoreTrustManager}
import io.phdata.clients.LDAPClientImpl

trait LDAPTest {
  val lookupClient = new LDAPClientImpl[IO](itestConfig.ldap, _.lookupBinding)
  val provisioningClient = new LDAPClientImpl[IO](itestConfig.ldap, _.provisioningBinding)

  val ldapConnectionPool: LDAPConnectionPool = {
    val sslUtil = if(itestConfig.ldap.ignoreSslCert.getOrElse(false)) {
      new SSLUtil(new TrustAllTrustManager)
    } else {
      new SSLUtil(new TrustStoreTrustManager(System.getProperty("javax.net.ssl.trustStore")))
    }

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

