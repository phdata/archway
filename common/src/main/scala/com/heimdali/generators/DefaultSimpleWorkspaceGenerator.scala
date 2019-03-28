package com.heimdali.generators

import java.time.Instant

import cats.effect.{Clock, Sync}
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models._

class DefaultSimpleWorkspaceGenerator[F[_]](appConfig: AppConfig,
                                            ldapGenerator: LDAPGroupGenerator[F],
                                            applicationGenerator: ApplicationGenerator[F],
                                            topicGenerator: TopicGenerator[F])
                                           (implicit clock: Clock[F], val F: Sync[F])
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

  override def workspaceFor(simpleTemplate: SimpleTemplate): F[WorkspaceRequest] = {
    val generatedName = WorkspaceGenerator.generateName(simpleTemplate.name)

    for {
      time <- clock.realTime(scala.concurrent.duration.MILLISECONDS)

      workspace = WorkspaceRequest(
        simpleTemplate.name,
        simpleTemplate.summary,
        simpleTemplate.description,
        "simple",
        simpleTemplate.requester,
        Instant.ofEpochMilli(time),
        simpleTemplate.compliance,
        singleUser = false,
        processing = List(Yarn(
          s"${appConfig.workspaces.sharedWorkspace.poolParents}.sw_$generatedName",
          appConfig.workspaces.sharedWorkspace.defaultCores,
          appConfig.workspaces.sharedWorkspace.defaultMemory)))

      manager <- ldapGenerator.generate(
        s"edh_sw_$generatedName",
        s"cn=edh_sw_$generatedName,${appConfig.ldap.groupPath}",
        s"role_sw_$generatedName",
        workspace)
      readonly <- ldapGenerator.generate(
        s"edh_sw_${generatedName}_ro",
        s"cn=edh_sw_${generatedName}_ro,${appConfig.ldap.groupPath}",
        s"role_sw_${generatedName}_ro",
        workspace)
      app <- applicationGenerator.applicationFor("default", workspace)
    } yield workspace.copy(
      applications = List(app),
      data = List(HiveAllocation(
        s"sw_$generatedName",
        s"${appConfig.workspaces.sharedWorkspace.root}/$generatedName",
        appConfig.workspaces.sharedWorkspace.defaultSize,
        manager,
        Some(readonly))),
    )
  }

}