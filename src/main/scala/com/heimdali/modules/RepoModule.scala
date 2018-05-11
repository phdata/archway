package com.heimdali.modules

import com.heimdali.repositories._
import scalikejdbc.ConnectionPool

trait RepoModule {
  this: ExecutionContextModule
    with ConfigurationModule =>

  val metaConfig = configuration.getConfig("db.meta")

  Class.forName(metaConfig.getString("driver"))
  ConnectionPool.add('default,
    metaConfig.getString("url"),
    metaConfig.getString("user"),
    metaConfig.getString("password"))

  val sharedWorkspaceRepository: SharedWorkspaceRepository = new SharedWorkspaceRepositoryImpl

  val accountRepository: UserWorkspaceRepository = new UserWorkspaceRepositoryImpl

  val complianceRepository: ComplianceRepository = new ComplianceRepositoryImpl

  val governedDatasetRepository: GovernedDatasetRepository = new GovernedDatasetRepositoryImpl()

  val datasetRepository: DatasetRepository = new DatasetRepositoryImpl()

  val ldapRepository: LDAPRepository = new LDAPRepositoryImpl()

  val hiveDatabaseRepository: HiveDatabaseRepository = new HiveDatabaseRepositoryImpl()

  val userWorkspaceRepository: UserWorkspaceRepository = new UserWorkspaceRepositoryImpl()

  val yarnRepository: YarnRepository = new YarnRepositoryImpl()

}
