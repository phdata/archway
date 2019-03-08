package com.heimdali.templates

import java.time.Clock

import cats.effect.Sync
import cats.implicits._
import com.heimdali.config.{AppConfig, LDAPConfig}
import com.heimdali.models._

class DefaultUserWorkspaceGenerator[F[_]](appConfig: AppConfig)
                                         (implicit val clock: Clock, val F: Sync[F])
  extends WorkspaceGenerator[F, UserTemplate]
    with AttributeGenerator[F] {

  override val ldapConfig: LDAPConfig = appConfig.ldap

  override def defaults(user: User): F[UserTemplate] =
    F.pure(
      UserTemplate(user.distinguishedName, user.username, Some(appConfig.workspaces.user.defaultSize), Some(appConfig.workspaces.sharedWorkspace.defaultCores), Some(appConfig.workspaces.sharedWorkspace.defaultMemory))
    )

  override def workspaceFor(template: UserTemplate): F[WorkspaceRequest] =
    for {
      managerHive <- generate(s"user_${template.username}",
                              s"cn=user_${template.username},${appConfig.ldap.groupPath}",
                              s"role_user_${template.username}")
    } yield WorkspaceRequest(
      template.username,
      template.username,
      template.username,
      "user",
      template.userDN,
      clock.instant(),
      Compliance(phiData = false, pciData = false, piiData = false),
      singleUser = true,
      data = List(HiveAllocation(
        s"user_${template.username}",
        s"${appConfig.workspaces.user.root}/${template.username}/db",
        appConfig.workspaces.user.defaultSize,
        managerHive,
        None
      )),
      processing = List(Yarn(
        s"${appConfig.workspaces.user.poolParents}.${template.username}",
        appConfig.workspaces.user.defaultCores,
        appConfig.workspaces.user.defaultMemory)))

}
