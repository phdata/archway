package com.heimdali.modules

import java.time.Clock

import com.heimdali.repositories._

trait RepoModule {

  val complianceRepository: ComplianceRepository = new ComplianceRepositoryImpl

  val ldapRepository: LDAPRepository = new LDAPRepositoryImpl(Clock.systemUTC())

  val hiveDatabaseRepository: HiveDatabaseRepository = new HiveDatabaseRepositoryImpl(Clock.systemUTC())

  val yarnRepository: YarnRepository = new YarnRepositoryImpl

  val workspaceRepository: WorkspaceRequestRepository = new WorkspaceRequestRepositoryImpl

  val approvalRepository: ApprovalRepository = new ApprovalRepositoryImpl

  val memberRepository: MemberRepository = new MemberRepositoryImpl

  val hiveGrantRepository: HiveGrantRepository = new HiveGrantRepositoryImpl(Clock.systemUTC())

}
