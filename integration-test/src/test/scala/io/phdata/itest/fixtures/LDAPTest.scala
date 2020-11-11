package io.phdata.itest.fixtures

import cats.effect.IO
import com.unboundid.ldap.sdk.{FailoverServerSet, LDAPConnectionPool, SimpleBindRequest}
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager, TrustStoreTrustManager}
import io.phdata.clients.{LDAPClientImpl, LookupLDAPClient, ProvisioningLDAPClient, RoleClientImpl}
import io.phdata.models.LDAPRegistration
import org.scalatest.Matchers

trait LDAPTest extends Matchers with HiveTest {
  val lookupClient: LookupLDAPClient[IO] = new LDAPClientImpl[IO](itestConfig.ldap, _.lookupBinding)
  val provisioningClient: ProvisioningLDAPClient[IO] = new LDAPClientImpl[IO](itestConfig.ldap, _.provisioningBinding)
  val helperLDAPClient = new LDAPClientImpl[IO](itestConfig.ldap, _.provisioningBinding)

  val roleClient = new RoleClientImpl[IO](hiveTransactor)

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
    roleClient.roles.unsafeRunSync() should contain (ldapRegistration.securityRole)
    roleClient.groupRoles(ldapRegistration.commonName).unsafeRunSync() should contain (ldapRegistration.securityRole)

    // clean up
    roleClient.revokeGroup(ldapRegistration.commonName, ldapRegistration.securityRole).unsafeRunSync()
    roleClient.dropRole(ldapRegistration.securityRole).unsafeRunSync()
    provisioningClient.deleteGroup(ldapRegistration.distinguishedName).value.unsafeRunSync()
  }
}

