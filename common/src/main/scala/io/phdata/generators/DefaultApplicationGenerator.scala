package io.phdata.generators

import cats.Monad
import cats.effect.Clock
import cats.implicits._
import io.phdata.config.AppConfig
import io.phdata.models.{Application, DistinguishedName, TemplateRequest, WorkspaceRequest}
import io.phdata.services.ApplicationRequest

class DefaultApplicationGenerator[F[_]](appConfig: AppConfig, ldapGroupGenerator: LDAPGroupGenerator[F])(
    implicit clock: Clock[F],
    F: Monad[F]
) extends ApplicationGenerator[F] {

  override def applicationFor(application: ApplicationRequest, workspace: WorkspaceRequest): F[Application] = {
    val consumerGroup = s"${TemplateRequest.generateName(workspace.name)}_${application.name}_cg"
    ldapGroupGenerator
      .generate(
        consumerGroup,
        DistinguishedName(s"cn=$consumerGroup,${appConfig.ldap.groupPath}"),
        s"role_$consumerGroup",
        workspace
      )
      .map { ldap =>
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
