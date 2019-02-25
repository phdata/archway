package com.heimdali.templates

import java.time.Clock

import cats.effect.Sync
import com.heimdali.config.AppConfig
import com.heimdali.models.{User, UserTemplate, WorkspaceRequest}

class TestTemplateGenerator[F[_]](appConfig: AppConfig)(implicit clock: Clock, F: Sync[F])
  extends TemplateGenerator[F, UserTemplate] {

  override def defaults(user: User): F[UserTemplate] = ???

  override def workspaceFor(template: UserTemplate): F[WorkspaceRequest] = ???

}
