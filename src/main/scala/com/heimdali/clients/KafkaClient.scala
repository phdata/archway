package com.heimdali.clients

import cats.effect.Sync
import com.typesafe.scalalogging.LazyLogging
import kafka.admin.AdminUtils
import kafka.utils.ZkUtils

trait KafkaClient[F[_]] {

  def createTopic(name: String, partitions: Int, replicationFactor: Int): F[Unit]

}

class KafkaClientImpl[F[_] : Sync](val zkConfig: ZkUtils)
  extends KafkaClient[F]
  with LazyLogging {

  override def createTopic(name: String, partitions: Int, replicationFactor: Int): F[Unit] = {
    logger.warn("creating {} via {}", name, zkConfig)
    Sync[F].delay(AdminUtils.createTopic(zkConfig, name, partitions, replicationFactor))
  }

}