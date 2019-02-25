package com.heimdali.services

import cats.effect._
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.models.Application
import com.heimdali.templates.TemplateGenerator
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._

class ApplicationServiceImpl[F[_]](appContext: AppContext[F])
                                  (implicit F: Effect[F])
  extends ApplicationService[F]
    with LazyLogging {

  override def create(username: String, workspaceId: Long, applicationRrequest: ApplicationRequest): F[Application] =
    for {
      workspace <-
        appContext
          .workspaceRequestRepository
          .find(workspaceId)
          .value
          .transact(appContext.transactor)

      _ <- F.pure(logger.warn("found {}", workspace))

      app = Application(
        username,
        TemplateGenerator.generateName(workspace.get.name),
        applicationRrequest.name,
        appContext.appConfig.ldap.groupPath
      )

      saved <- (for {
        ldap <- appContext.ldapRepository.create(app.group)
        beforeSave = app.copy(group = ldap)
        appId <- appContext.applicationRepository.create(beforeSave)
        _ <- appContext.memberRepository.create(username, ldap.id.get)
        _ <- appContext.workspaceRequestRepository.linkApplication(workspaceId, appId)
      } yield beforeSave.copy(id = Some(appId))).transact(appContext.transactor)
    } yield saved

}
