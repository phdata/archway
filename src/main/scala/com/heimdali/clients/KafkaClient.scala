package com.heimdali.clients

import cats.effect.Sync
import kafka.admin.AdminUtils
import kafka.utils.ZkUtils

trait KafkaClient[F[_]] {

  def createTopic(name: String, partitions: Int, replicationFactor: Int): F[Unit]

}

class KafkaClientImpl[F[_] : Sync](zkConfig: ZkUtils) extends KafkaClient[F] {

  override def createTopic(name: String, partitions: Int, replicationFactor: Int): F[Unit] =
    Sync[F].delay(AdminUtils.createTopic(zkConfig, name, partitions, replicationFactor))

}