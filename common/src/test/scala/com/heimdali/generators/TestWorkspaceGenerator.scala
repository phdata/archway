package com.heimdali.generators

import cats.effect.{Clock, Sync}
import com.heimdali.config.AppConfig
import com.heimdali.models.{User, UserTemplate, WorkspaceRequest}

class TestWorkspaceGenerator[F[_]](appConfig: AppConfig,
                                   ldapGroupGenerator: LDAPGroupGenerator[F],
                                   applicationGenerator: ApplicationGenerator[F])
                                  (implicit clock: Clock[F], F: Sync[F])
  extends WorkspaceGenerator[F, UserTemplate] {

  override def defaults(user: User): F[UserTemplate] = ???

  override def workspaceFor(template: UserTemplate): F[WorkspaceRequest] = ???

}
