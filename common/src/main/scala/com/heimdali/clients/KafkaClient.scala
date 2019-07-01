package com.heimdali.clients

import cats.effect.Sync
import com.heimdali.config.AppConfig
import com.typesafe.scalalogging.LazyLogging
import kafka.admin.AdminUtils
import kafka.utils.ZkUtils
import kafka.utils.ZKStringSerializer$
import org.I0Itec.zkclient.{ZkClient, ZkConnection}

trait KafkaClient[F[_]] {

  def createTopic(name: String, partitions: Int, replicationFactor: Int): F[Unit]

  def deleteTopic(name: String): F[Unit]

}

class KafkaClientImpl[F[_]: Sync](appConfig: AppConfig) extends KafkaClient[F] with LazyLogging {

  val sessionTimeOutInMs = 15 * 1000; // 15 secs
  val connectionTimeOutInMs = 10 * 1000; // 10 secs
  val zkClient = new ZkClient(
    appConfig.kafka.zookeeperConnect,
    sessionTimeOutInMs,
    connectionTimeOutInMs,
    ZKStringSerializer$.MODULE$
  )
  val zkUtils = new ZkUtils(zkClient, new ZkConnection(appConfig.kafka.zookeeperConnect), false)

  override def createTopic(name: String, partitions: Int, replicationFactor: Int): F[Unit] = {
    logger.warn("creating {} via {}", name, zkUtils)
    Sync[F].delay(AdminUtils.createTopic(zkUtils, name, partitions, replicationFactor))
  }

  override def deleteTopic(name: String): F[Unit] =
    Sync[F].delay(AdminUtils.deleteTopic(zkUtils, name))

}
