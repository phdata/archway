package com.heimdali

import cats.effect.Sync
import cats.implicits._
import java.util.Properties
import kafka.admin.{ AdminUtils, RackAwareMode }
import kafka.utils.ZkUtils


trait KafkaClient[F[_]] {
  def createTopic(name: String): F[Unit]
}

class KafkaClientImpl[F[_]: Sync](zkUtils: ZkUtils)
    extends KafkaClient[F] {

  def createTopic(name: String, partitions: Int, replicationFactor: Int): F[Unit] = Sync[F].delay {
    AdminUtils.createTopic(zkUtils, name, partitions, replicationFactor, new Properties(), RackAwareMode.Enforced)
  }.void

}
