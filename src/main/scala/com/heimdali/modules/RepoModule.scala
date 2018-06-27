package com.heimdali.modules

import com.heimdali.repositories._

trait RepoModule {

  val complianceRepository: ComplianceRepository = new ComplianceRepositoryImpl

  val ldapRepository: LDAPRepository = new LDAPRepositoryImpl

  val hiveDatabaseRepository: HiveDatabaseRepository = new HiveDatabaseRepositoryImpl

  val yarnRepository: YarnRepository = new YarnRepositoryImpl

  val workspaceRepository: WorkspaceRequestRepository = new WorkspaceRequestRepositoryImpl

  val approvalRepository: ApprovalRepository = new ApprovalRepositoryImpl

  val memberRepository: MemberRepository = new MemberRepositoryImpl

}
