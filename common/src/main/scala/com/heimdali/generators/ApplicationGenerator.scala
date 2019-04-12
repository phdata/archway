package com.heimdali.generators

import cats.effect.{Clock, Sync}
import com.heimdali.config.{AppConfig, GeneratorConfig}
import com.heimdali.models.{Application, WorkspaceRequest}
import com.heimdali.services.ApplicationRequest

trait ApplicationGenerator[F[_]] {

  def applicationFor(application: ApplicationRequest, workspace: WorkspaceRequest): F[Application]

}

object ApplicationGenerator {

  def instance[F[_]](appConfig: AppConfig, ldapGenerator: LDAPGroupGenerator[F], className: GeneratorConfig => String)
                    (implicit clock: Clock[F], F: Sync[F]): ApplicationGenerator[F] =
    Class
      .forName(className(appConfig.generators))
      .getConstructors
      .head
      .newInstance(appConfig, ldapGenerator, clock, F)
      .asInstanceOf[ApplicationGenerator[F]]

}
