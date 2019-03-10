package com.heimdali.generators

import java.time.Clock

import cats.effect.Sync
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models._

class DefaultUserWorkspaceGenerator[F[_]](appConfig: AppConfig,
                                          ldapGroupGenerator: LDAPGroupGenerator[F],
                                          applicationGenerator: ApplicationGenerator[F])
                                         (implicit val clock: Clock, val F: Sync[F])
  extends WorkspaceGenerator[F, UserTemplate] {

  override def defaults(user: User): F[UserTemplate] =
    F.pure(
      UserTemplate(user.distinguishedName, user.username, Some(appConfig.workspaces.user.defaultSize), Some(appConfig.workspaces.sharedWorkspace.defaultCores), Some(appConfig.workspaces.sharedWorkspace.defaultMemory))
    )

  override def workspaceFor(template: UserTemplate): F[WorkspaceRequest] = {
    val workspace = WorkspaceRequest(
      template.username,
      template.username,
      template.username,
      "user",
      template.userDN,
      clock.instant(),
      Compliance(phiData = false, pciData = false, piiData = false),
      singleUser = true,
      processing = List(Yarn(
        s"${appConfig.workspaces.user.poolParents}.${template.username}",
        appConfig.workspaces.user.defaultCores,
        appConfig.workspaces.user.defaultMemory)))

    ldapGroupGenerator
      .generate(
        s"user_${template.username}",
        s"cn=user_${template.username},${appConfig.ldap.groupPath}",
        s"role_user_${template.username}",
        workspace).map { managerHive =>
      workspace.copy(
        data = List(HiveAllocation(
          s"user_${template.username}",
          s"${appConfig.workspaces.user.root}/${template.username}/db",
          appConfig.workspaces.user.defaultSize,
          managerHive,
          None
        ))
      )
    }
  }

}
