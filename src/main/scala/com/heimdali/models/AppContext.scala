package com.heimdali.models

import com.heimdali.clients._
import com.heimdali.repositories.{HiveDatabaseRepository, LDAPRepository}
import doobie.util.transactor.Transactor
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClient

case class AppContext[F[_]](hiveClient: HiveClient[F],
                            ldapClient: LDAPClient[F],
                            hdfsClient: HDFSClient[F],
                            yarnClient: YarnClient[F],
                            kafkaClient: KafkaClient[F],
                            sentryClient: SentryGenericServiceClient,
                            transactor: Transactor[F],
                            databaseRepository: HiveDatabaseRepository,
                            ldapRepository: LDAPRepository)
