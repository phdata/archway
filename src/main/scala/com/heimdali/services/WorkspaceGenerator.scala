package com.heimdali.services

import java.time.Instant

import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

sealed trait Template

case class UserTemplate(username: String, disk: Option[Int], cores: Option[Int], memory: Option[Int]) extends Template

object UserTemplate {
  implicit val encoder: Encoder[UserTemplate] = deriveEncoder
  implicit val decoder: Decoder[UserTemplate] = deriveDecoder
}

case class SimpleTemplate(name: String, summary: String, description: String, requester: String, compliance: Compliance, disk: Option[Int], cores: Option[Int], memory: Option[Int]) extends Template

object SimpleTemplate {
  implicit val encoder: Encoder[SimpleTemplate] = deriveEncoder
  implicit val decoder: Decoder[SimpleTemplate] = deriveDecoder
}

case class StructuredTemplate(name: String, summary: String, description: String, requester: String, compliance: Compliance, includeEnvironment: Boolean, disk: Option[Int], cores: Option[Int], memory: Option[Int]) extends Template

object StructuredTemplate {
  implicit val encoder: Encoder[StructuredTemplate] = deriveEncoder
  implicit val decoder: Decoder[StructuredTemplate] = deriveDecoder
}

trait Generator[T] {
  def generate(t: T): WorkspaceRequest

  def defaults(user: User): T
}

object Generator {

  import pureconfig._

  implicit def hint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  val Right(appConfig) = pureconfig.loadConfig[AppConfig]

  def generateName(name: String): String =
    name
      .replaceAll("""[^a-zA-Z\d\s]""", " ") //replace with space to remove multiple underscores
      .trim //if we have leading or trailing spaces, clean them up
      .replaceAll("""\s+""", "_")
      .toLowerCase

  implicit class GeneratorOps[A](a: A) {
    def generate()(implicit generator: Generator[A]) = {
      generator.generate(a)
    }
  }

  def apply[A](implicit ev: Generator[A]): Generator[A] = ev

  implicit object UserGenerator extends Generator[UserTemplate] {
    override def defaults(user: User): UserTemplate =
      UserTemplate(user.username, Some(appConfig.workspaces.user.defaultSize), Some(appConfig.workspaces.sharedWorkspace.defaultCores), Some(appConfig.workspaces.sharedWorkspace.defaultMemory))

    override def generate(input: UserTemplate): WorkspaceRequest = {
      val request = WorkspaceRequest(
        input.username,
        input.username,
        input.username,
        "user",
        input.username,
        Instant.now(),
        Compliance(phiData = false, pciData = false, piiData = false),
        singleUser = true)
      val afterDisk = input.disk.fold(request) { _ =>
        request.copy(data = List(HiveDatabase(
          s"user_${input.username}",
          s"${appConfig.workspaces.user.root}/${input.username}/db",
          0,
          appConfig.workspaces.user.defaultSize,
          LDAPRegistration(s"cn=user_${input.username},${appConfig.ldap.groupPath}", s"user_${input.username}", s"role_user_${input.username}"),
          None
        )))
      }
      val afterProcessing = (input.memory |+| input.cores).fold(afterDisk) { _ =>
        afterDisk.copy(processing = List(Yarn(
          s"${appConfig.workspaces.user.poolParents}.${input.username}",
          appConfig.workspaces.user.defaultCores,
          appConfig.workspaces.user.defaultMemory)))
      }
      afterProcessing
    }
  }

  implicit object SimpleGenerator extends Generator[SimpleTemplate] {
    override def defaults(user: User): SimpleTemplate =
      SimpleTemplate(s"${user.username}'s Workspace", "A brief summary", "A longer description", user.username, Compliance.empty, Some(appConfig.workspaces.sharedWorkspace.defaultSize), Some(appConfig.workspaces.sharedWorkspace.defaultCores), Some(appConfig.workspaces.sharedWorkspace.defaultMemory))

    override def generate(input: SimpleTemplate): WorkspaceRequest = {
      val generatedName = generateName(input.name)
      val request = WorkspaceRequest(
        input.name,
        input.summary,
        input.description,
        "simple",
        input.requester,
        Instant.now(),
        input.compliance,
        singleUser = false)
      val afterDisk = input.disk.fold(request) { _ =>
        request.copy(data = List(HiveDatabase(
          s"sw_$generatedName",
          s"${appConfig.workspaces.sharedWorkspace.root}/$generatedName",
          appConfig.workspaces.sharedWorkspace.defaultSize,
          0,
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
      val afterProcessing = (input.memory |+| input.cores).fold(afterDisk) { _ =>
        afterDisk.copy(processing = List(Yarn(
          s"${appConfig.workspaces.sharedWorkspace.poolParents}.sw_$generatedName",
          appConfig.workspaces.sharedWorkspace.defaultCores,
          appConfig.workspaces.sharedWorkspace.defaultMemory)))
      }
      afterProcessing
    }
  }

  implicit object GovernedGenerator extends Generator[StructuredTemplate] {
    override def defaults(user: User): StructuredTemplate =
      StructuredTemplate(s"${user.username}'s Workspace", "A brief summary", "A longer description", user.username, Compliance.empty, includeEnvironment = false, Some(appConfig.workspaces.dataset.defaultSize), Some(appConfig.workspaces.dataset.defaultCores), Some(appConfig.workspaces.dataset.defaultMemory))

    override def generate(input: StructuredTemplate): WorkspaceRequest = {
      val generatedName = generateName(input.name)
      val request = WorkspaceRequest(
        input.name,
        input.summary,
        input.description,
        "structured",
        input.requester,
        Instant.now(),
        input.compliance,
        singleUser = false)

      def db(disk: Int, dataset: String) =
        HiveDatabase(
          s"${dataset}_$generatedName",
          s"${appConfig.workspaces.dataset.root}/$dataset/$generatedName",
          disk,
          0,
          LDAPRegistration(
            s"cn=edh_${appConfig.cluster.environment}_${dataset}_$generatedName,${appConfig.ldap.groupPath}",
            s"edh_${appConfig.cluster.environment}_${dataset}_$generatedName",
            s"role_${appConfig.cluster.environment}_${dataset}_$generatedName"),
          Some(LDAPRegistration(
            s"cn=edh_${appConfig.cluster.environment}_${dataset}_${generatedName}_ro,${appConfig.ldap.groupPath}",
            s"edh_${appConfig.cluster.environment}_${dataset}_${generatedName}_ro",
            s"role_${appConfig.cluster.environment}_${dataset}_${generatedName}_ro")))

      val afterDisk = input.disk.fold(request) { disk =>
        request.copy(data = List(db(disk, "raw"), db(disk, "staging"), db(disk, "modeled")))
      }
      val afterProcessing = (input.memory |+| input.cores).fold(afterDisk) { _ =>
        afterDisk.copy(processing = List(Yarn(
          s"${appConfig.workspaces.dataset.poolParents}.governed_$generatedName",
          input.cores.getOrElse(appConfig.workspaces.dataset.defaultCores),
          input.memory.getOrElse(appConfig.workspaces.dataset.defaultMemory))))
      }
      afterProcessing
    }
  }

}