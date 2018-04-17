package com.heimdali.modules

import cats.Eval
import cats.data.IndexedStateT
import cats.effect.IO
import doobie.FC
import com.heimdali.services._
import doobie.util.transactor.{Strategy, Transactor}

trait ServiceModule {
  this: AkkaModule
    with ExecutionContextModule
    with RepoModule
    with ClientModule
    with RepoModule
    with ConfigurationModule
    with HttpModule =>

  val hiveConfig = configuration.getConfig("db.hive")

  val rawHiveTransactor = Transactor.fromDriverManager[IO](
    hiveConfig.getString("driver"),
    hiveConfig.getString("url"),
    hiveConfig.getString("user"),
    hiveConfig.getString("password")
  )

  val hiveTransactor: Transactor[IO] = {
    val xa = (for {
      _ <- Transactor.before[IO] := FC.unit
      _ <- Transactor.after[IO] := FC.unit
    } yield ()).runS(rawHiveTransactor).value
    Transactor.strategy.set(xa, Strategy.void.copy(always = FC.close))
  }

  val accountService: AccountService = new AccountServiceImpl(ldapClient, accountRepository, configuration)

  val clusterService: ClusterService = new CDHClusterService(http, configuration)

  val workspaceService: WorkspaceService = new WorkspaceServiceImpl(workspaceRepository, workspaceProvisionerFactory)

  val keytabService: KeytabService = new KeytabServiceImpl()

  val hiveService: HiveService = new HiveServiceImpl(hiveTransactor)
}
