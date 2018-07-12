package com.heimdali.modules

import com.unboundid.ldap.sdk.{LDAPConnection, LDAPConnectionPool}
import java.net.URI

import com.heimdali.clients._
import com.heimdali.services._
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.hdfs.client.HdfsAdmin

trait ClientModule[F[_]] {
  this: AppModule[F]
    with ConfigurationModule
    with HttpModule[F]
    with ContextModule[F]
    with FileSystemModule[F]
    with ClusterModule[F] =>

  val ldapConnectionPool: LDAPConnectionPool = {
    val connection = new LDAPConnection(
      appConfig.ldap.server,
      appConfig.ldap.port,
      appConfig.ldap.bindDN,
      appConfig.ldap.bindPassword
    )
    new LDAPConnectionPool(connection, 10)
  }

  val hdfsUri = new URI(hadoopConfiguration.get("fs.defaultFS"))

  def fileSystemLoader(): FileSystem =
    FileSystem.get(hadoopConfiguration)

  val hdfsAdmin: () => HdfsAdmin = () =>
    new HdfsAdmin(hdfsUri, hadoopConfiguration)

  val ldapClient: LDAPClient[F] =
    new LDAPClientImpl(appConfig.ldap, () => ldapConnectionPool.getConnection)
    with ActiveDirectoryClient[F]

  val hdfsClient: HDFSClient[F] =
    new HDFSClientImpl[F](fileSystemLoader, hdfsAdmin, loginContextProvider)

  val yarnClient: YarnClient[F] =
    new CDHYarnClient[F](http, appConfig.cluster, clusterService)

  val kafkaClient: KafkaClient[F] = ???

}
