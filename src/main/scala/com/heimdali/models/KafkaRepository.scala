package com.heimdali.models

import cats.data.OptionT
import doobie.free.connection.ConnectionIO

trait KafkaRepository {

  def create(workspaceId: Long, kafkaTopic: KafkaTopic): ConnectionIO[Long]

  def topicCreated(id: Long): ConnectionIO[Int]

  def find(id: Long): OptionT[ConnectionIO, KafkaTopic]

  def list(workspaceId: Long): ConnectionIO[List[KafkaTopic]]

}
