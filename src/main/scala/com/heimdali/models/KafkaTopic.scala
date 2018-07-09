package com.heimdali.models

import cats.effect.IO
import com.heimdali.clients._
import com.heimdali.repositories.ProvisionAttemptRepository
import doobie.util.transactor.Transactor
import org.apache.sentry.provider.db.service.thrift.SentryPolicyServiceClient

case class KafkaTopic(name: String, partitions: Int, replicationFactor: Int)

case class AppConfig(hiveClient: HiveClient[IO],
                     sentryClient: SentryPolicyServiceClient,
                     ldapClient: LDAPClient[IO],
                     hdfsClient: HDFSClient[IO],
                     yarnClient: YarnClient[IO],
                     kafkaClient: KafkaClient[IO],
                     transactor: Transactor[IO],
                     provisionAttemptRepository: ProvisionAttemptRepository)