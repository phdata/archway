package com.heimdali.modules

import com.heimdali.repositories.{AccountRepository, AccountRepositoryImpl, WorkspaceRepository, WorkspaceRepositoryImpl}
import scalikejdbc.config.DBs
import scalikejdbc.{DB, DBSession, GlobalSettings, LoggingSQLAndTimeSettings, SettingsProvider}

trait RepoModule {
  this: ExecutionContextModule =>

  val workspaceRepository: WorkspaceRepository = new WorkspaceRepositoryImpl

  val accountRepository: AccountRepository = new AccountRepositoryImpl

  val session: DBSession = {
    DBs.loadGlobalSettings()
    DBs.setupAll()
    GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
      enabled = true,
      singleLineMode = false,
      printUnprocessedStackTrace = false,
      stackTraceDepth= 15,
      logLevel = 'debug,
      warningEnabled = false,
      warningThresholdMillis = 3000L,
      warningLogLevel = 'warn
    )
    DB.autoCommitSession(SettingsProvider.default.copy(
      jtaDataSourceCompatible = e => true,
      loggingSQLAndTime = e => e.copy(enabled = true, logLevel = 'debug)
    ))
  }

}
