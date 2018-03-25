package com.heimdali.modules

import cats.effect.IO
import com.heimdali.repositories.{AccountRepository, AccountRepositoryImpl, WorkspaceRepository, WorkspaceRepositoryImpl}
import doobie.util.transactor.Transactor

trait RepoModule {
  this: ExecutionContextModule
    with ConfigurationModule =>

  val metaConfig = configuration.getConfig("db.meta")

  val transactor = Transactor.fromDriverManager[IO](
    metaConfig.getString("driver"),
    metaConfig.getString("url"),
    metaConfig.getString("user"),
    metaConfig.getString("password")
  )

  val workspaceRepository: WorkspaceRepository = new WorkspaceRepositoryImpl(transactor)

  val accountRepository: AccountRepository = new AccountRepositoryImpl(transactor)

}
