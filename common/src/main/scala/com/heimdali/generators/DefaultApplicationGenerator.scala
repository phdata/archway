package com.heimdali.generators

import cats.Monad
import cats.effect.Clock
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models.{Application, WorkspaceRequest}
import com.heimdali.services.{ApplicationRequest, TemplateService}

class DefaultApplicationGenerator[F[_]](appConfig: AppConfig,
                                        ldapGroupGenerator: LDAPGroupGenerator[F])
                                       (implicit clock: Clock[F], F: Monad[F])
  extends ApplicationGenerator[F] {

  override def applicationFor(application: ApplicationRequest, workspace: WorkspaceRequest): F[Application] = {
    val consumerGroup = s"${TemplateService.generateName(workspace.name)}_${application.name}_cg"
    ldapGroupGenerator
      .generate(
        consumerGroup,
        s"cn=$consumerGroup,${appConfig.ldap.groupPath}",
        s"role_$consumerGroup",
        workspace).map { ldap =>
      Application(
        application.name,
        consumerGroup,
        ldap,
        application.applicationType,
        application.logo,
        application.language,
        application.repository
      )
    }
  }

}
