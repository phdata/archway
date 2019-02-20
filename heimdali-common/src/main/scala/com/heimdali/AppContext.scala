package com.heimdali

import com.heimdali.clients._
import com.heimdali.config.AppConfig
import com.heimdali.repositories._
import doobie.Transactor

case class AppContext[F[_]](appConfig: AppConfig,
                            sentryClient: SentryClient[F],
                            hiveClient: HiveClient[F],
                            ldapClient: LDAPClient[F],
                            hdfsClient: HDFSClient[F],
                            yarnClient: YarnClient[F],
                            kafkaClient: KafkaClient[F],
                            transactor: Transactor[F],
                            databaseRepository: HiveAllocationRepository,
                            databaseGrantRepository: HiveGrantRepository,
                            ldapRepository: LDAPRepository,
                            memberRepository: MemberRepository,
                            yarnRepository: YarnRepository,
                            complianceRepository: ComplianceRepository,
                            workspaceRequestRepository: WorkspaceRequestRepository,
                            kafkaRepository: KafkaTopicRepository,
                            topicGrantRepository: TopicGrantRepository,
                            applicationRepository: ApplicationRepository)
