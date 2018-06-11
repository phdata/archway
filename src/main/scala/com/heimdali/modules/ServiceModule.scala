package com.heimdali.modules

import java.sql.{Connection, DriverManager}

import com.heimdali.clients.{HiveClient, HiveClientImpl}
import com.heimdali.config
import com.heimdali.services._
import doobie._
import doobie.util.transactor.{Strategy, Transactor}

trait ServiceModule[F[_]] {
  this: AppModule[F]
    with ExecutionContextModule
    with ClientModule[F]
    with RepoModule
    with ConfigurationModule
    with HttpModule[F] =>

  val hiveConfig = appConfig.db.hive

  private val initialHiveTransactor = Transactor.fromDriverManager[F](hiveConfig.driver, hiveConfig.url, "", "")
  val strategy = Strategy.void.copy(always = FC.close)
  val hiveTransactor = Transactor.strategy.set(initialHiveTransactor, strategy)

  private val metaConfig: config.DatabaseConfigItem = appConfig.db.meta

  private val metaTransactor = Transactor.fromDriverManager[F](
    metaConfig.driver,
    metaConfig.url,
    metaConfig.username.get,
    metaConfig.password.get
  )

  val keytabService: KeytabService[F] =
    new KeytabServiceImpl[F]()

  val hiveService: HiveClient[F] =
    new HiveClientImpl[F](hiveTransactor)

  val environment: String =
    configuration.getString("cluster.environment")

  val accountService: AccountService[F] =
    new AccountServiceImpl[F](ldapClient, appConfig.rest, appConfig.approvers)

  val hiveConnectionFactory: () => Connection =
    () => DriverManager.getConnection(appConfig.db.hive.url, appConfig.db.hive.username.getOrElse(""), appConfig.db.hive.password.getOrElse(""))

  val workspaceService: WorkspaceService[F] =
    new WorkspaceServiceImpl[F](
      ldapClient,
      hdfsClient,
      hiveService,
      yarnClient,
      yarnRepository,
      hiveDatabaseRepository,
      ldapRepository,
      workspaceRepository,
      complianceRepository,
      hiveConnectionFactory,
      metaTransactor)
}
