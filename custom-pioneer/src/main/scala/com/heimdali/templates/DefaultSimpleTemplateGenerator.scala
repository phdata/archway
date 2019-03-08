package com.heimdali.templates

import java.time.Clock

import cats.effect.Sync
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models._

class DefaultSimpleTemplateGenerator[F[_]](appConfig: AppConfig)
                                          (implicit clock: Clock, F: Sync[F])
  extends WorkspaceGenerator[F, SimpleTemplate] {

  override def defaults(user: User): F[SimpleTemplate] =
    F.pure(
      SimpleTemplate(
        s"${user.username}'s Workspace",
        "A brief summary",
        "A longer description",
        user.distinguishedName,
        Compliance.empty,
        Some(appConfig.workspaces.sharedWorkspace.defaultSize),
        Some(appConfig.workspaces.sharedWorkspace.defaultCores),
        Some(appConfig.workspaces.sharedWorkspace.defaultMemory)
      )
    )

  override def workspaceFor(simpleTemplate: SimpleTemplate): F[WorkspaceRequest] =
    F.pure {
      val generatedName = WorkspaceGenerator.generateName(simpleTemplate.name)
      val request = WorkspaceRequest(
        simpleTemplate.name,
        simpleTemplate.summary,
        simpleTemplate.description,
        "simple",
        simpleTemplate.requester,
        clock.instant(),
        simpleTemplate.compliance,
        applications = List(Application(
          "default",
          s"${generatedName}_default_cg",
          LDAPRegistration(
            s"cn=${generatedName}_default_cg,${appConfig.ldap.groupPath}",
            s"${generatedName}_default_cg",
            s"role_${generatedName}_default_cg"
          ))),
        singleUser = false)
      val afterDisk = simpleTemplate.disk.fold(request) {
        _ =>
          request.copy(data = List(HiveAllocation(
            s"sw_$generatedName",
            s"${appConfig.workspaces.sharedWorkspace.root}/$generatedName",
            appConfig.workspaces.sharedWorkspace.defaultSize,
            LDAPRegistration(
              s"cn=edh_sw_$generatedName,${appConfig.ldap.groupPath}",
              s"edh_sw_$generatedName",
              s"role_sw_$generatedName"),
            Some(
              LDAPRegistration(
                s"cn=edh_sw_${generatedName}_ro,${appConfig.ldap.groupPath}",
                s"edh_sw_${generatedName}_ro",
                s"role_sw_${generatedName}_ro")))))
      }
      val afterProcessing = (simpleTemplate.memory |+| simpleTemplate.cores).fold(afterDisk) {
        _ =>
          afterDisk.copy(processing = List(Yarn(
            s"${appConfig.workspaces.sharedWorkspace.poolParents}.sw_$generatedName",
            appConfig.workspaces.sharedWorkspace.defaultCores,
            appConfig.workspaces.sharedWorkspace.defaultMemory)))
      }
      afterProcessing
    }
}
