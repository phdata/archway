package com.heimdali.templates

import java.time.Clock

import cats.effect.Sync
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models._

class DefaultStructuredTemplateGenerator[F[_]](appConfig: AppConfig)
                                              (implicit clock: Clock, F: Sync[F])
  extends TemplateGenerator[F, StructuredTemplate] {

    override def defaults(user: User): F[StructuredTemplate] =
      F.pure(
        StructuredTemplate(s"${user.username}'s Workspace", "A brief summary", "A longer description", user.distinguishedName, Compliance.empty, includeEnvironment = false, Some(appConfig.workspaces.dataset.defaultSize), Some(appConfig.workspaces.dataset.defaultCores), Some(appConfig.workspaces.dataset.defaultMemory))
      )

    override def workspaceFor(structuredTemplate: StructuredTemplate): F[WorkspaceRequest] =
      F.pure {
        val generatedName = TemplateGenerator.generateName(structuredTemplate.name)
        val request = WorkspaceRequest(
          structuredTemplate.name,
          structuredTemplate.summary,
          structuredTemplate.description,
          "structured",
          structuredTemplate.requester,
          clock.instant(),
          structuredTemplate.compliance,
          singleUser = false)

        def db(disk: Int, dataset: String) =
          HiveAllocation(
            s"${dataset}_$generatedName",
            s"${appConfig.workspaces.dataset.root}/$dataset/$generatedName",
            disk,
            LDAPRegistration(
              s"cn=edh_${appConfig.cluster.environment}_${dataset}_$generatedName,${appConfig.ldap.groupPath}",
              s"edh_${appConfig.cluster.environment}_${dataset}_$generatedName",
              s"role_${appConfig.cluster.environment}_${dataset}_$generatedName"),
            Some(LDAPRegistration(
              s"cn=edh_${appConfig.cluster.environment}_${dataset}_${generatedName}_ro,${appConfig.ldap.groupPath}",
              s"edh_${appConfig.cluster.environment}_${dataset}_${generatedName}_ro",
              s"role_${appConfig.cluster.environment}_${dataset}_${generatedName}_ro")))

        val afterDisk = structuredTemplate.disk.fold(request) {
          disk =>
            request.copy(data = List(db(disk, "raw"), db(disk, "staging"), db(disk, "modeled")))
        }
        val afterProcessing = (structuredTemplate.memory |+| structuredTemplate.cores).fold(afterDisk) {
          _ =>
            afterDisk.copy(processing = List(Yarn(
              s"${appConfig.workspaces.dataset.poolParents}.governed_$generatedName",
              structuredTemplate.cores.getOrElse(appConfig.workspaces.dataset.defaultCores),
              structuredTemplate.memory.getOrElse(appConfig.workspaces.dataset.defaultMemory))))
        }
        afterProcessing
      }
  }
