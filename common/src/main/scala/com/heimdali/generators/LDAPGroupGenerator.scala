package com.heimdali.generators

import cats.effect.{Clock, Sync}
import com.heimdali.config.{AppConfig, GeneratorConfig}
import com.heimdali.models.{LDAPRegistration, WorkspaceRequest}
import com.heimdali.services.ConfigService

trait LDAPGroupGenerator[F[_]] {

  def generate(cn: String, dn: String, role: String, workspace: WorkspaceRequest): F[LDAPRegistration]

}

object LDAPGroupGenerator {

  def instance[F[_]](appConfig: AppConfig, configService: ConfigService[F], className: GeneratorConfig => String)
                    (implicit clock: Clock[F], F: Sync[F]): LDAPGroupGenerator[F] =
    Class
      .forName(className(appConfig.generators))
      .getConstructors
      .head
      .newInstance(appConfig, configService, clock, F)
      .asInstanceOf[LDAPGroupGenerator[F]]

}