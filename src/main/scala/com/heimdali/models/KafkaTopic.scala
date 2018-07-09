package com.heimdali.models

import cats.effect.IO
import com.heimdali.clients.{HiveClient, LDAPClient}
import org.apache.sentry.provider.db.service.thrift.SentryPolicyServiceClient

case class KafkaTopic(name: String, partitions: Int, replicationFactor: Int)

case class AppConfig(hiveClient: HiveClient[IO], sentryService: SentryPolicyServiceClient, lDAPClient: LDAPClient[IO])