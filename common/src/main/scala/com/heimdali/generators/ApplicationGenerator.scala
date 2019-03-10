package com.heimdali.generators

import java.time.Clock

import cats.effect.Sync
import com.heimdali.config.{AppConfig, GeneratorConfig}
import com.heimdali.models.{Application, WorkspaceRequest}

trait ApplicationGenerator[F[_]] {

  def applicationFor(name: String, workspace: WorkspaceRequest): F[Application]

}

object ApplicationGenerator {

  def instance[F[_]](appConfig: AppConfig, ldapGenerator: LDAPGroupGenerator[F], className: GeneratorConfig => String)
                    (implicit clock: Clock, F: Sync[F]): ApplicationGenerator[F] =
    Class
      .forName(className(appConfig.generators))
      .getConstructors
      .head
      .newInstance(appConfig, ldapGenerator, clock, F)
      .asInstanceOf[ApplicationGenerator[F]]

}
