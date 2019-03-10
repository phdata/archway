package com.heimdali.generators

import java.time.Clock

import cats.Monad
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models.{Application, WorkspaceRequest}

class DefaultApplicationGenerator[F[_]](appConfig: AppConfig,
                                        ldapGroupGenerator: LDAPGroupGenerator[F])
                                       (implicit clock: Clock, F: Monad[F])
  extends ApplicationGenerator[F] {

  override def applicationFor(name: String, workspace: WorkspaceRequest): F[Application] = {
    val consumerGroup = s"${WorkspaceGenerator.generateName(workspace.name)}_${name}_cg"
    ldapGroupGenerator
      .generate(
        consumerGroup,
        s"cn=$consumerGroup,${appConfig.ldap.groupPath}",
        s"role_$consumerGroup",
        workspace).map { ldap =>
      Application(
        name,
        consumerGroup,
        ldap
      )
    }
  }

}
