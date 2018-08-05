package com.heimdali.models

import com.heimdali.clients._
import com.heimdali.repositories._
import com.heimdali.config.AppConfig
import doobie.util.transactor.Transactor
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClient

case class AppContext[F[_]](appConfig: AppConfig,
                            hiveClient: SentryClient[F],
                            ldapClient: LDAPClient[F],
                            hdfsClient: HDFSClient[F],
                            yarnClient: YarnClient[F],
                            kafkaClient: KafkaClient[F],
                            transactor: Transactor[F],
                            databaseRepository: HiveDatabaseRepository,
                            databaseGrantRepository: HiveGrantRepository,
                            ldapRepository: LDAPRepository,
                            memberRepository: MemberRepository,
                            yarnRepository: YarnRepository,
                            complianceRepository: ComplianceRepository,
                            workspaceRequestRepository: WorkspaceRequestRepository,
                            kafkaRepository: KafkaTopicRepository,
                            topicGrantRepository: TopicGrantRepository,
                            applicationRepository: ApplicationRepository)
