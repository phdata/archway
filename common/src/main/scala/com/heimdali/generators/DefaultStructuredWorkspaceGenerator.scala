package com.heimdali.generators

import java.time.Clock

import cats.effect.Sync
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models._

class DefaultStructuredWorkspaceGenerator[F[_]](appConfig: AppConfig,
                                                ldapGenerator: LDAPGroupGenerator[F],
                                                applicationGenerator: ApplicationGenerator[F])
                                               (implicit clock: Clock, val F: Sync[F])
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
      Some(readonly))

  override def workspaceFor(structuredTemplate: StructuredTemplate): F[WorkspaceRequest] = {
    val generatedName = WorkspaceGenerator.generateName(structuredTemplate.name)

    val workspace = WorkspaceRequest(
      structuredTemplate.name,
      structuredTemplate.summary,
      structuredTemplate.description,
      "structured",
      structuredTemplate.requester,
      clock.instant(),
      structuredTemplate.compliance,
      singleUser = false,
      processing = List(Yarn(
        s"${appConfig.workspaces.dataset.poolParents}.governed_$generatedName",
        structuredTemplate.cores.getOrElse(appConfig.workspaces.dataset.defaultCores),
        structuredTemplate.memory.getOrElse(appConfig.workspaces.dataset.defaultMemory))))

    for {
      raw <- db("raw", generatedName, structuredTemplate, workspace)
      staging <- db("staging", generatedName, structuredTemplate, workspace)
      modeled <- db("modeled", generatedName, structuredTemplate, workspace)
    } yield workspace.copy(data = List(raw, staging, modeled))
  }
}
