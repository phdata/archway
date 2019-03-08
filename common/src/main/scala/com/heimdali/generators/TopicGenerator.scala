package com.heimdali.generators

import java.time.Clock

import cats.effect.Sync
import com.heimdali.config.{AppConfig, GeneratorConfig}
import com.heimdali.models.KafkaTopic

trait TopicGenerator[F[_]] {

  def topicFor(name: String, partitions: Int, replicationFactor: Int, workspaceSystemName: String): F[KafkaTopic]

}

object TopicGenerator {

  def instance[F[_]](appConfig: AppConfig, className: GeneratorConfig => String)
                    (implicit clock: Clock, F: Sync[F]): TopicGenerator[F] =
    Class
      .forName(className(appConfig.generators))
      .getConstructors
      .head
      .newInstance(appConfig, F)
      .asInstanceOf[TopicGenerator[F]]

}
