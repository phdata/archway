package io.phdata.generators

import cats.effect.{Clock, Sync}
import io.phdata.config.AppConfig
import io.phdata.models.{KafkaTopic, WorkspaceRequest}

trait TopicGenerator[F[_]] {

  def topicFor(name: String, partitions: Int, replicationFactor: Int, workspaceRequest: WorkspaceRequest): F[KafkaTopic]

}

object TopicGenerator {

  def instance[F[_]](appConfig: AppConfig, ldapGroupGenerator: LDAPGroupGenerator[F], className: String)(
      implicit clock: Clock[F],
      F: Sync[F]
  ): TopicGenerator[F] =
    Class
      .forName(className)
      .getConstructors
      .head
      .newInstance(appConfig, ldapGroupGenerator, clock, F)
      .asInstanceOf[TopicGenerator[F]]

}
