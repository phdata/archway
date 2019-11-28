package io.phdata.itest.fixtures

import cats.effect.IO
import com.unboundid.ldap.sdk.{FailoverServerSet, LDAPConnectionPool, SimpleBindRequest}
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager, TrustStoreTrustManager}
import io.phdata.clients.{LDAPClientImpl, LookupLDAPClient, ProvisioningLDAPClient, SentryClientImpl}
import io.phdata.models.LDAPRegistration
import io.phdata.services.UGILoginContextProvider
import org.scalatest.Matchers

trait LDAPTest extends Matchers with HiveTest {
  val lookupClient: LookupLDAPClient[IO] = new LDAPClientImpl[IO](itestConfig.ldap, _.lookupBinding)
  val provisioningClient: ProvisioningLDAPClient[IO] = new LDAPClientImpl[IO](itestConfig.ldap, _.provisioningBinding)
  val helperLDAPClient = new LDAPClientImpl[IO](itestConfig.ldap, _.provisioningBinding)

  val sentryClient = new SentryClientImpl[IO](hiveTransactor, null, new UGILoginContextProvider(itestConfig))

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

  def validateLdapRegistrationProvisioning(ldapRegistration: LDAPRegistration) = {
    //verification
    Option(ldapConnectionPool.getConnection.getEntry(ldapRegistration.distinguishedName.value)) shouldBe defined
    sentryClient.roles.unsafeRunSync() should contain (ldapRegistration.sentryRole)
    sentryClient.groupRoles(ldapRegistration.commonName).unsafeRunSync() should contain (ldapRegistration.sentryRole)

    // clean up
    sentryClient.revokeGroup(ldapRegistration.commonName, ldapRegistration.sentryRole).unsafeRunSync()
    sentryClient.dropRole(ldapRegistration.sentryRole).unsafeRunSync()
    provisioningClient.deleteGroup(ldapRegistration.distinguishedName).value.unsafeRunSync()
  }
}

