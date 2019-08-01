package io.phdata.clients

import cats.effect.Sync
import io.phdata.config.AppConfig
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

  val SESSION_TIMEOUT_MS = 15 * 1000; // 15 secs
  val CONNECTION_TIMEOUT_MS = 10 * 1000; // 10 secs
  val MIN_REPLICATION_FACTOR = 3

  val zkClient = new ZkClient(
    appConfig.kafka.zookeeperConnect,
    SESSION_TIMEOUT_MS,
    CONNECTION_TIMEOUT_MS,
    ZKStringSerializer$.MODULE$
  )
  val zkUtils = new ZkUtils(zkClient, new ZkConnection(appConfig.kafka.zookeeperConnect), false)

  override def createTopic(name: String, partitions: Int, replicationFactor: Int): F[Unit] = {
    logger.warn("creating {} via {}", name, zkUtils)
    val computedReplicationFactor = Math.max(replicationFactor, MIN_REPLICATION_FACTOR)
    Sync[F].delay(AdminUtils.createTopic(zkUtils, name, partitions, computedReplicationFactor))
  }

  override def deleteTopic(name: String): F[Unit] =
    Sync[F].delay(AdminUtils.deleteTopic(zkUtils, name))

}
