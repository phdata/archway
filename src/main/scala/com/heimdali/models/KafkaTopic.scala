package com.heimdali.models

import com.heimdali.clients._

case class KafkaTopic(name: String, partitions: Int, replicationFactor: Int)

case class AppConfig[F[_]](hiveClient: HiveClient[F],
                           ldapClient: LDAPClient[F],
                           hdfsClient: HDFSClient[F],
                           yarnClient: YarnClient[F],
                           kafkaClient: KafkaClient[F])