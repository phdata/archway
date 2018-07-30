package com.heimdali.modules

import com.unboundid.ldap.sdk.{LDAPConnection, LDAPConnectionPool}
import java.net.URI

import cats.effect.Sync
import com.heimdali.clients._
import com.heimdali.services._
import com.typesafe.scalalogging.LazyLogging
import doobie.FC
import doobie.util.transactor.{Strategy, Transactor}
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.hdfs.client.HdfsAdmin
import org.apache.sentry.provider.db.generic.service.thrift.{SentryGenericServiceClient, SentryGenericServiceClientFactory}

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

  val kafkaClient: KafkaClient[F] =
    new KafkaClientImpl[F](zkUtils)

  val hiveConfig = appConfig.db.hive
  Class.forName("org.apache.hive.jdbc.HiveDriver")
  // Turn the transactor into no
  private val initialHiveTransactor =
    Transactor.fromDriverManager[F](hiveConfig.driver, hiveConfig.url, "", "")
  val strategy = Strategy.void.copy(always = FC.close)
  val hiveTransactor = Transactor.strategy.set(initialHiveTransactor, strategy)

  private lazy val sentryServiceClient: SentryGenericServiceClient =
    SentryGenericServiceClientFactory.create(hadoopConfiguration)

  val sentryClient: SentryClient[F] =
    new SentryClient[F] with LazyLogging {
      override def grantPrivilege(role: String, component: Component, grantString: String): F[Unit] =
        Sync[F].pure(logger.warn("granting {} permissions {} for {}", role, grantString, component))

      override def createRole(name: String): F[Unit] =
        Sync[F].pure(logger.warn("creating role {}", name))

      override def createDatabase(name: String, location: String): F[Unit] =
        Sync[F].pure(logger.warn("creating database {} at {}", name, location))

      override def enableAccessToDB(database: String, role: String): F[Unit] =
        Sync[F].pure(logger.warn("allowing {} access to {}", role, database))

      override def grantGroup(group: String, role: String): F[Unit] =
        Sync[F].pure(logger.warn("granting group {} access to role {}", group, role))

      override def enableAccessToLocation(location: String, role: String): F[Unit] =
        Sync[F].pure(logger.warn("allowing {} access to {}", role, location))
    }

}
