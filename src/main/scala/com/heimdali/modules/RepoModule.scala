package com.heimdali.modules

import java.time.Clock

import cats.effect.IO

import com.heimdali.repositories._

trait RepoModule {
  self: AppModule[IO] =>

  val complianceRepository: ComplianceRepository = new ComplianceRepositoryImpl

  val ldapRepository: LDAPRepository = new LDAPRepositoryImpl(clock)

  val hiveDatabaseRepository: HiveDatabaseRepository = new HiveDatabaseRepositoryImpl(clock)

  val yarnRepository: YarnRepository = new YarnRepositoryImpl(clock)

  val workspaceRepository: WorkspaceRequestRepository = new WorkspaceRequestRepositoryImpl

  val approvalRepository: ApprovalRepository = new ApprovalRepositoryImpl

  val memberRepository: MemberRepository = new MemberRepositoryImpl

  val hiveGrantRepository: HiveGrantRepository = new HiveGrantRepositoryImpl(clock)

  val kafkaRepository: KafkaRepository = new KafkaRepositoryImpl(clock)

  val topicGrantRepository: TopicGrantRepository = new TopicGrantRepositoryImpl(clock)

}
