package com.heimdali.generators

import java.time.Clock

import cats.effect.Sync
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models._

class DefaultUserTemplateGenerator[F[_]](appConfig: AppConfig)
                                        (implicit clock: Clock, F: Sync[F])
  extends WorkspaceGenerator[F, UserTemplate] {

    override def defaults(user: User): F[UserTemplate] =
      F.pure(
        UserTemplate(user.distinguishedName, user.username, Some(appConfig.workspaces.user.defaultSize), Some(appConfig.workspaces.sharedWorkspace.defaultCores), Some(appConfig.workspaces.sharedWorkspace.defaultMemory))
      )

    override def workspaceFor(template: UserTemplate): F[WorkspaceRequest] =
      F.pure {
        val request = WorkspaceRequest(
          template.username,
          template.username,
          template.username,
          "user",
          template.userDN,
          clock.instant(),
          Compliance(phiData = false, pciData = false, piiData = false),
          singleUser = true)
        val afterDisk = template.disk.fold(request) { _ =>
          request.copy(data = List(HiveAllocation(
            s"user_${template.username}",
            s"${appConfig.workspaces.user.root}/${template.username}/db",
            appConfig.workspaces.user.defaultSize,
            LDAPRegistration(s"cn=user_${template.username},${appConfig.ldap.groupPath}", s"user_${template.username}", s"role_user_${template.username}"),
            None
          )))
        }
        val afterProcessing = (template.memory |+| template.cores).fold(afterDisk) { _ =>
          afterDisk.copy(processing = List(Yarn(
            s"${appConfig.workspaces.user.poolParents}.${template.username}",
            appConfig.workspaces.user.defaultCores,
            appConfig.workspaces.user.defaultMemory)))
        }
        afterProcessing
      }

  }
