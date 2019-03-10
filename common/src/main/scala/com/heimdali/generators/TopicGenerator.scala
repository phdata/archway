package com.heimdali.generators

import java.time.Clock

import cats.effect.Sync
import com.heimdali.config.{AppConfig, GeneratorConfig}
import com.heimdali.models.{KafkaTopic, WorkspaceRequest}

trait TopicGenerator[F[_]] {

  def topicFor(name: String, partitions: Int, replicationFactor: Int, workspaceRequest: WorkspaceRequest): F[KafkaTopic]

}

object TopicGenerator {

  def instance[F[_]](appConfig: AppConfig, ldapGroupGenerator: LDAPGroupGenerator[F], className: GeneratorConfig => String)
                    (implicit clock: Clock, F: Sync[F]): TopicGenerator[F] =
    Class
      .forName(className(appConfig.generators))
      .getConstructors
      .head
      .newInstance(appConfig, ldapGroupGenerator, clock, F)
      .asInstanceOf[TopicGenerator[F]]

}
