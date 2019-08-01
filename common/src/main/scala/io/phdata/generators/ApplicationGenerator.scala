package io.phdata.generators

import cats.effect.{Clock, Sync}
import io.phdata.config.AppConfig
import io.phdata.models.{Application, WorkspaceRequest}
import io.phdata.services.ApplicationRequest

trait ApplicationGenerator[F[_]] {

  def applicationFor(application: ApplicationRequest, workspace: WorkspaceRequest): F[Application]

}

object ApplicationGenerator {

  def instance[F[_]](appConfig: AppConfig, ldapGenerator: LDAPGroupGenerator[F], className: String)(
      implicit clock: Clock[F],
      F: Sync[F]
  ): ApplicationGenerator[F] =
    Class
      .forName(className)
      .getConstructors
      .head
      .newInstance(appConfig, ldapGenerator, clock, F)
      .asInstanceOf[ApplicationGenerator[F]]

}
