package com.heimdali.modules

import cats.effect.IO
import com.heimdali.repositories._

trait RepoModule {
  self: ConfigurationModule =>

  val complianceRepository: ComplianceRepository = new ComplianceRepositoryImpl

  val ldapRepository: LDAPRepository = new LDAPRepositoryImpl(clock)

  val hiveDatabaseRepository: HiveAllocationRepository = new HiveAllocationRepositoryImpl(clock)

  val yarnRepository: YarnRepository = new YarnRepositoryImpl(clock)

  val workspaceRepository: WorkspaceRequestRepository = new WorkspaceRequestRepositoryImpl

  val approvalRepository: ApprovalRepository = new ApprovalRepositoryImpl

  val memberRepository: MemberRepository = new MemberRepositoryImpl

  val hiveGrantRepository: HiveGrantRepository = new HiveGrantRepositoryImpl(clock)

  val topicRepository: KafkaTopicRepository = new KafkaTopicRepositoryImpl(clock)

  val topicGrantRepository: TopicGrantRepository = new TopicGrantRepositoryImpl(clock)

  val applicationRepository: ApplicationRepository = new ApplicationRepositoryImpl(clock)

  val configRepository: ConfigRepository = new ConfigRepositoryImpl()

}
