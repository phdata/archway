package com.heimdali.generators

import java.time.Clock

import cats.effect.Sync
import com.heimdali.config.{AppConfig, GeneratorConfig}
import com.heimdali.models.Application

trait ApplicationGenerator[F[_]] {

  def applicationFor(name: String, workspaceSystemName: String): F[Application]

}

object ApplicationGenerator {

  def instance[F[_]](appConfig: AppConfig, className: GeneratorConfig => String)
                    (implicit clock: Clock, F: Sync[F]): ApplicationGenerator[F] =
    Class
      .forName(className(appConfig.generators))
      .getConstructors
      .head
      .newInstance(appConfig, F)
      .asInstanceOf[ApplicationGenerator[F]]

}
