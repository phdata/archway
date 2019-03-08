package com.heimdali.templates

import java.time.Clock

import cats.effect.Sync
import cats.implicits._
import com.heimdali.config
import com.heimdali.config.AppConfig
import com.heimdali.models._

class DefaultSimpleWorkspaceGenerator[F[_]](appConfig: AppConfig,
                                            applicationGenerator: ApplicationGenerator[F])
                                           (implicit clock: Clock, val F: Sync[F])
  extends WorkspaceGenerator[F, SimpleTemplate]
    with AttributeGenerator[F] {

  override def ldapConfig: config.LDAPConfig = appConfig.ldap

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

  override def workspaceFor(simpleTemplate: SimpleTemplate): F[WorkspaceRequest] = {
    val generatedName = WorkspaceGenerator.generateName(simpleTemplate.name)

    for {
      manager <- generate(
        s"edh_sw_$generatedName",
        s"cn=edh_sw_$generatedName,${appConfig.ldap.groupPath}",
        s"role_sw_$generatedName")
      readonly <- generate(
        s"edh_sw_${generatedName}_ro",
        s"cn=edh_sw_${generatedName}_ro,${appConfig.ldap.groupPath}",
        s"role_sw_${generatedName}_ro")
      app <- applicationGenerator.applicationFor("default", generatedName)
    } yield WorkspaceRequest(
      simpleTemplate.name,
      simpleTemplate.summary,
      simpleTemplate.description,
      "simple",
      simpleTemplate.requester,
      clock.instant(),
      simpleTemplate.compliance,
      applications = List(app),
      singleUser = false,
      data = List(HiveAllocation(
        s"sw_$generatedName",
        s"${appConfig.workspaces.sharedWorkspace.root}/$generatedName",
        appConfig.workspaces.sharedWorkspace.defaultSize,
        manager,
        Some(readonly))),
      processing = List(Yarn(
        s"${appConfig.workspaces.sharedWorkspace.poolParents}.sw_$generatedName",
        appConfig.workspaces.sharedWorkspace.defaultCores,
        appConfig.workspaces.sharedWorkspace.defaultMemory)))
  }

}