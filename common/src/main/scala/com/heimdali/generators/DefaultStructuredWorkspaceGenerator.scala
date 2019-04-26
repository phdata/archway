package com.heimdali.generators

import java.time.Instant

import cats.effect.{Clock, Sync}
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models._
import com.heimdali.services.ApplicationRequest

class DefaultStructuredWorkspaceGenerator[F[_]](appConfig: AppConfig,
                                                ldapGenerator: LDAPGroupGenerator[F],
                                                applicationGenerator: ApplicationGenerator[F],
                                                topicGenerator: TopicGenerator[F])
                                               (implicit clock: Clock[F], val F: Sync[F])
  extends WorkspaceGenerator[F, StructuredTemplate] {

  override def defaults(user: User): F[StructuredTemplate] =
    F.pure(
      StructuredTemplate(s"${user.username}'s Workspace", "A brief summary", "A longer description", user.distinguishedName, Compliance.empty, includeEnvironment = false, Some(appConfig.workspaces.dataset.defaultSize), Some(appConfig.workspaces.dataset.defaultCores), Some(appConfig.workspaces.dataset.defaultMemory))
    )

  private def db(dataset: String, generatedName: String, structuredTemplate: StructuredTemplate, workspaceRequest: WorkspaceRequest): F[HiveAllocation] =
    for {
      manager <- ldapGenerator.generate(
        s"edh_${appConfig.cluster.environment}_${dataset}_$generatedName",
        s"cn=edh_${appConfig.cluster.environment}_${dataset}_$generatedName,${appConfig.ldap.groupPath}",
        s"role_${appConfig.cluster.environment}_${dataset}_$generatedName",
        workspaceRequest)
      readwrite <- ldapGenerator.generate(
        s"edh_${appConfig.cluster.environment}_${dataset}_${generatedName}_rw",
        s"cn=edh_${appConfig.cluster.environment}_${dataset}_${generatedName}_rw,${appConfig.ldap.groupPath}",
        s"role_${appConfig.cluster.environment}_${dataset}_${generatedName}_rw",
        workspaceRequest)
      readonly <- ldapGenerator.generate(
        s"edh_${appConfig.cluster.environment}_${dataset}_${generatedName}_ro",
        s"cn=edh_${appConfig.cluster.environment}_${dataset}_${generatedName}_ro,${appConfig.ldap.groupPath}",
        s"role_${appConfig.cluster.environment}_${dataset}_${generatedName}_ro",
        workspaceRequest)
    } yield HiveAllocation(
      s"${dataset}_$generatedName",
      s"${appConfig.workspaces.dataset.root}/$dataset/$generatedName",
      structuredTemplate.disk.get,
      manager,
      Some(readwrite),
      Some(readonly))

  override def workspaceFor(structuredTemplate: StructuredTemplate): F[WorkspaceRequest] = {
    val generatedName = WorkspaceGenerator.generateName(structuredTemplate.name)

    for {
      time <- clock.realTime(scala.concurrent.duration.MILLISECONDS)

      workspace = WorkspaceRequest(
        structuredTemplate.name,
        structuredTemplate.summary,
        structuredTemplate.description,
        "structured",
        structuredTemplate.requester,
        Instant.ofEpochMilli(time),
        structuredTemplate.compliance,
        singleUser = false,
        processing = List(Yarn(
          s"${appConfig.workspaces.dataset.poolParents}.governed_$generatedName",
          structuredTemplate.cores.getOrElse(appConfig.workspaces.dataset.defaultCores),
          structuredTemplate.memory.getOrElse(appConfig.workspaces.dataset.defaultMemory))))

      raw <- db("raw", generatedName, structuredTemplate, workspace)
      staging <- db("staging", generatedName, structuredTemplate, workspace)
      modeled <- db("modeled", generatedName, structuredTemplate, workspace)
      application <- applicationGenerator.applicationFor(ApplicationRequest("default"), workspace)
    } yield workspace.copy(data = List(raw, staging, modeled), applications = List(application))
  }
}
