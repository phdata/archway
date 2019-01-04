package com.heimdali.modules

import java.net.URI

import cats.effect.Effect
import com.heimdali.clients._
import com.heimdali.config.SMTPConfig
import com.unboundid.ldap.sdk.{LDAPConnection, LDAPConnectionPool}
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager}
import courier.Mailer
import doobie.FC
import doobie.util.transactor.{Strategy, Transactor}
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.hdfs.client.HdfsAdmin
import org.apache.sentry.provider.db.generic.service.thrift.{SentryGenericServiceClient, SentryGenericServiceClientFactory}

trait ClientModule[F[_]] {
  this: ConfigurationModule
    with ExecutionContextModule[F]
    with HttpModule[F]
    with ContextModule[F]
    with FileSystemModule[F]
    with ClusterModule[F] =>

  implicit def effect: Effect[F]

  val ldapConnectionPool: LDAPConnectionPool = {
    val sslUtil = new SSLUtil(new TrustAllTrustManager)
    val sslSocketFactory = sslUtil.createSSLSocketFactory
    val connection = new LDAPConnection(
      sslSocketFactory,
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
    Transactor.fromDriverManager[F](hiveConfig.driver, hiveConfig.url, "", "")(effect, contextShift)
  val strategy = Strategy.void.copy(always = FC.close)
  val hiveTransactor = Transactor.strategy.set(initialHiveTransactor, strategy)

  private lazy val sentryServiceClient: SentryGenericServiceClient =
    SentryGenericServiceClientFactory.create(hadoopConfiguration)

  val sentryClient: SentryClient[F] =
    new SentryClientImpl[F](hiveTransactor, sentryServiceClient, loginContextProvider)

  val hiveClient: HiveClient[F] =
    new HiveClientImpl[F](loginContextProvider, hiveTransactor)

  private val mailer: Mailer = appConfig.smtp match {
    case SMTPConfig(host, port, true, Some(user), Some(pass), ssl) =>
      Mailer(host, port)
        .auth(true)
        .as(user, pass)
        .startTls(ssl)()

    case SMTPConfig(host, port, _, _, _, ssl) =>
      Mailer(host, port)
        .startTls(ssl)()
  }

  val emailClient: EmailClient[F] =
    new EmailClientImpl[F](mailer)(effect, executionContext)

}
