package com.heimdali.generators

import java.time.Clock

import cats.effect.Sync
import com.heimdali.config.{AppConfig, GeneratorConfig}
import com.heimdali.models.{LDAPRegistration, WorkspaceRequest}

trait LDAPGroupGenerator[F[_]] {

  def generate(cn: String, dn: String, role: String, workspace: WorkspaceRequest): F[LDAPRegistration]

}

object LDAPGroupGenerator {

  def instance[F[_]](appConfig: AppConfig, className: GeneratorConfig => String)
                                                   (implicit clock: Clock, F: Sync[F]): LDAPGroupGenerator[F] =
    Class
      .forName(className(appConfig.generators))
      .getConstructors
      .head
      .newInstance(appConfig, clock, F)
      .asInstanceOf[LDAPGroupGenerator[F]]

}