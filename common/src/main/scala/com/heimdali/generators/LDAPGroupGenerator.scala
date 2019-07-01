package com.heimdali.generators

import cats.effect.{Clock, Sync}
import com.heimdali.config.AppConfig
import com.heimdali.models.{LDAPRegistration, WorkspaceRequest}
import com.heimdali.services.ConfigService

trait LDAPGroupGenerator[F[_]] {

  def generate(cn: String, dn: String, role: String, workspace: WorkspaceRequest): F[LDAPRegistration]

}

object LDAPGroupGenerator {

  def instance[F[_]](appConfig: AppConfig, configService: ConfigService[F], className: String)(
      implicit clock: Clock[F],
      F: Sync[F]
  ): LDAPGroupGenerator[F] =
    Class
      .forName(className)
      .getConstructors
      .head
      .newInstance(configService, clock, F)
      .asInstanceOf[LDAPGroupGenerator[F]]

}
