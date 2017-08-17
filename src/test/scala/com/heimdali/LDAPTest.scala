package com.heimdali

import com.unboundid.ldap.listener.{InMemoryDirectoryServer, InMemoryDirectoryServerConfig, InMemoryListenerConfig}

trait LDAPTest {
  def ldapPort: Int

  val baseDN = "dc=jotunn,dc=io"
  val userDN = "ou=edp"
  val bindDN = "cn=readonly,dc=jotunn,dc=io"
  val bindPassword = "readonly"
  val username = "username"
  val password = "password"

  lazy val inMemoryServer: InMemoryDirectoryServer = {
    val listenerConfig = new InMemoryListenerConfig("test", null, ldapPort, null, null, null)
    val ldapConfig = new InMemoryDirectoryServerConfig(baseDN)
    ldapConfig.setListenerConfigs(listenerConfig)
    ldapConfig.setSchema(null)
    val server = new InMemoryDirectoryServer(ldapConfig)
    val file = getClass.getResource("/basicUser.ldif")
    server.importFromLDIF(false, file.getPath)
    server
  }
}
