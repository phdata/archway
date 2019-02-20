package com.heimdali.services

import java.time.Clock

import cats.effect._
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models._

trait TemplateService[F[_]] {

  def userDefaults(user: User): F[UserTemplate]

  def userWorkspace(userTemplate: UserTemplate): F[WorkspaceRequest]

  def simpleDefaults(user: User): F[SimpleTemplate]

  def simpleWorkspace(simpleTemplate: SimpleTemplate): F[WorkspaceRequest]

  def structuredDefaults(user: User): F[StructuredTemplate]

  def structuredWorkspace(structuredTemplate: StructuredTemplate): F[WorkspaceRequest]

  def generateName(name: String): String =
    name
      .replaceAll("""[^a-zA-Z\d\s]""", " ") //replace with space to remove multiple underscores
      .trim //if we have leading or trailing spaces, clean them up
      .replaceAll("""\s+""", "_")
      .toLowerCase

}

class DefaultTemplateService[F[_]](appConfig: AppConfig)
                                  (implicit clock: Clock, F: Sync[F])
  extends TemplateService[F] {

  override def userDefaults(user: User): F[UserTemplate] =
    F.pure(
      UserTemplate(user.distinguishedName, user.username, Some(appConfig.workspaces.user.defaultSize), Some(appConfig.workspaces.sharedWorkspace.defaultCores), Some(appConfig.workspaces.sharedWorkspace.defaultMemory))
    )

  override def userWorkspace(userTemplate: UserTemplate): F[WorkspaceRequest] =
    F.pure {
      val request = WorkspaceRequest(
        userTemplate.username,
        userTemplate.username,
        userTemplate.username,
        "user",
        userTemplate.userDN,
        clock.instant(),
        Compliance(phiData = false, pciData = false, piiData = false),
        singleUser = true)
      val afterDisk = userTemplate.disk.fold(request) { _ =>
        request.copy(data = List(HiveAllocation(
          s"user_${userTemplate.username}",
          s"${appConfig.workspaces.user.root}/${userTemplate.username}/db",
          appConfig.workspaces.user.defaultSize,
          LDAPRegistration(s"cn=user_${userTemplate.username},${appConfig.ldap.groupPath}", s"user_${userTemplate.username}", s"role_user_${userTemplate.username}"),
          None
        )))
      }
      val afterProcessing = (userTemplate.memory |+| userTemplate.cores).fold(afterDisk) { _ =>
        afterDisk.copy(processing = List(Yarn(
          s"${appConfig.workspaces.user.poolParents}.${userTemplate.username}",
          appConfig.workspaces.user.defaultCores,
          appConfig.workspaces.user.defaultMemory)))
      }
      afterProcessing
    }

  override def simpleDefaults(user: User): F[SimpleTemplate] =
    F.pure(
      SimpleTemplate(s"${user.username}'s Workspace", "A brief summary", "A longer description", user.distinguishedName, Compliance.empty, Some(appConfig.workspaces.sharedWorkspace.defaultSize), Some(appConfig.workspaces.sharedWorkspace.defaultCores), Some(appConfig.workspaces.sharedWorkspace.defaultMemory))
    )

  override def simpleWorkspace(simpleTemplate: SimpleTemplate): F[WorkspaceRequest] =
    F.pure {
      val generatedName = generateName(simpleTemplate.name)
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
          LDAPRegistration(s"cn=${generatedName}_default_cg,${appConfig.ldap.groupPath}", s"${generatedName}_default_cg", s"role_${generatedName}_default_cg"),
          requestor = Some(simpleTemplate.requester)
        )),
        singleUser = false)
      val afterDisk = simpleTemplate.disk.fold(request) { _ =>
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
      val afterProcessing = (simpleTemplate.memory |+| simpleTemplate.cores).fold(afterDisk) { _ =>
        afterDisk.copy(processing = List(Yarn(
          s"${appConfig.workspaces.sharedWorkspace.poolParents}.sw_$generatedName",
          appConfig.workspaces.sharedWorkspace.defaultCores,
          appConfig.workspaces.sharedWorkspace.defaultMemory)))
      }
      afterProcessing
    }

  override def structuredDefaults(user: User): F[StructuredTemplate] =
    F.pure(
      StructuredTemplate(s"${user.username}'s Workspace", "A brief summary", "A longer description", user.distinguishedName, Compliance.empty, includeEnvironment = false, Some(appConfig.workspaces.dataset.defaultSize), Some(appConfig.workspaces.dataset.defaultCores), Some(appConfig.workspaces.dataset.defaultMemory))
    )

  override def structuredWorkspace(structuredTemplate: StructuredTemplate): F[WorkspaceRequest] =
    F.pure {
      val generatedName = generateName(structuredTemplate.name)
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

      val afterDisk = structuredTemplate.disk.fold(request) { disk =>
        request.copy(data = List(db(disk, "raw"), db(disk, "staging"), db(disk, "modeled")))
      }
      val afterProcessing = (structuredTemplate.memory |+| structuredTemplate.cores).fold(afterDisk) { _ =>
        afterDisk.copy(processing = List(Yarn(
          s"${appConfig.workspaces.dataset.poolParents}.governed_$generatedName",
          structuredTemplate.cores.getOrElse(appConfig.workspaces.dataset.defaultCores),
          structuredTemplate.memory.getOrElse(appConfig.workspaces.dataset.defaultMemory))))
      }
      afterProcessing
    }
}