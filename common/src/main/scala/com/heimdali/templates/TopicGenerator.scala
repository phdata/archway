package com.heimdali.templates

import com.heimdali.models.KafkaTopic

trait TopicGenerator[F[_]] {

  def topicFor(name: String, partitions: Int, replicationFactor: Int, workspaceSystemName: String): F[KafkaTopic]

}

