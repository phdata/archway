package com.heimdali

import java.net.URLEncoder
import java.util.concurrent.TimeUnit

import cats.effect.{Async, ContextShift, Resource}
import doobie._
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Strategy

import scala.concurrent.duration._
import io.circe.Decoder.Result
import io.circe.{Decoder, DecodingFailure, HCursor, Printer}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, FiniteDuration}

package object config {

  implicit final val finiteDurationDecoder: Decoder[Duration] =
    (c: HCursor) => {
      for {
        duration <- c.as[String]
      } yield Duration(duration)
    }

  case class CredentialsConfig(username: String, password: String)

  case class ServiceOverride(host: Option[String], port: Int)

  case class ClusterConfig(sessionRefresh: Duration,
                           url: String,
                           name: String,
                           environment: String,
                           beeswaxPort: Int,
                           hiveServer2Port: Int,
                           admin: CredentialsConfig,
                           hueOverride: ServiceOverride) {
    private val encodedName: String = URLEncoder.encode(name, "utf-8").replaceAll("\\+", "%20")

    val clusterUrl: String = s"$url/api/v18/clusters/$encodedName"

    def hostListUrl = s"$url/api/v18/hosts"

    def hostUrl(hostId: String) = s"$url/hosts/$hostId"

    val serviceListUrl = s"$clusterUrl/services"

    def serviceUrl(service: String) = s"$serviceListUrl/$service"

    def serviceConfigUrl(service: String) = s"${serviceUrl(service)}/config"

    def yarnApplications(service: String) = s"${serviceUrl(service)}/yarnApplications"

    def serviceRoleListUrl(service: String) = s"${serviceUrl(service)}/roles"

    def serviceRoleUrl(service: String, roleId: String) = s"${serviceRoleListUrl(service)}/$roleId"

    val refreshUrl = s"$clusterUrl/commands/poolsRefresh"
  }

  object ClusterConfig {

    import io.circe.generic.semiauto._

    implicit val credentialsConfigDecoder: Decoder[CredentialsConfig] = deriveDecoder
    implicit val serviceOverrideDecoder: Decoder[ServiceOverride] = deriveDecoder

    implicit val decoder: Decoder[ClusterConfig] = Decoder.instance { cursor =>
      println(cursor.value.pretty(Printer.spaces2))
      for {
        sessionRefresh <- cursor.downField("sessionRefresh").as[String].map(Duration.apply)
        url <- cursor.downField("url").as[String]
        name <- cursor.downField("name").as[String]
        environment <- cursor.downField("environment").as[String]
        beeswaxPort <- cursor.downField("beeswaxPort").as[Int]
        hiveServer2Port <- cursor.downField("hiveServer2Port").as[Int]
        admin <- cursor.downField("admin").as[CredentialsConfig]
        hueOverride <- cursor.downField("hueOverride").as[ServiceOverride]
      } yield ClusterConfig(sessionRefresh, url, name, environment, beeswaxPort, hiveServer2Port, admin, hueOverride)
    }

  }

  case class RestConfig(port: Int,
                        secret: String,
                        sslStore: Option[String] = None,
                        sslStorePassword: Option[String] = None,
                        sslKeyManagerPassword: Option[String] = None)

  case class UIConfig(url: String)

  case class SMTPConfig(fromEmail: String, host: String, port: Int, auth: Boolean, user: Option[String], pass: Option[String], ssl: Boolean)

  case class ApprovalConfig(notificationEmail: String, infrastructure: String, risk: String)

  case class WorkspaceConfigItem(root: String, defaultSize: Int, defaultCores: Int, defaultMemory: Int, poolParents: String)

  case class WorkspaceConfig(user: WorkspaceConfigItem, sharedWorkspace: WorkspaceConfigItem, dataset: WorkspaceConfigItem)

  case class LDAPConfig(server: String,
                        port: Int,
                        baseDN: String,
                        groupPath: String,
                        userPath: Option[String],
                        bindDN: String,
                        bindPassword: String,
                        domain: String,
                        realm: String)

  case class DatabaseConfigItem(driver: String, url: String, username: Option[String], password: Option[String]) {

    def hiveTx[F[_] : Async : ContextShift]: Transactor[F] = {
      Class.forName("org.apache.hive.jdbc.HiveDriver")

      // Turn the transactor into no
      val initialHiveTransactor = Transactor.fromDriverManager[F](driver, url, "", "")
      val strategy = Strategy.void.copy(always = FC.close)

      Transactor.strategy.set(initialHiveTransactor, strategy)
    }

    def tx[F[_] : Async : ContextShift](connectionEC: ExecutionContext, transactionEC: ExecutionContext): Resource[F, HikariTransactor[F]] =
      HikariTransactor.newHikariTransactor[F](driver, url, username.getOrElse(""), password.getOrElse(""), connectionEC, transactionEC)

  }

  case class DatabaseConfig(meta: DatabaseConfigItem, hive: DatabaseConfigItem)

  case class KafkaConfig(zookeeperConnect: String)

  case class GeneratorConfig(userGenerator: String,
                             simpleGenerator: String,
                             structuredGenerator: String,
                             topicGenerator: String,
                             applicationGenerator: String,
                             ldapGroupGenerator: String)

  case class AppConfig(generators: GeneratorConfig,
                       rest: RestConfig,
                       ui: UIConfig,
                       smtp: SMTPConfig,
                       cluster: ClusterConfig,
                       approvers: ApprovalConfig,
                       ldap: LDAPConfig,
                       db: DatabaseConfig,
                       workspaces: WorkspaceConfig,
                       kafka: KafkaConfig)

  object AppConfig {

    import io.circe.generic.semiauto._

    implicit val restConfigDecoder: Decoder[RestConfig] = deriveDecoder
    implicit val uIConfigDecoder: Decoder[UIConfig] = deriveDecoder
    implicit val sMTPConfigDecoder: Decoder[SMTPConfig] = deriveDecoder
    implicit val approvalConfigDecoder: Decoder[ApprovalConfig] = deriveDecoder
    implicit val workspaceConfigItemDecoder: Decoder[WorkspaceConfigItem] = deriveDecoder
    implicit val workspaceConfigDecoder: Decoder[WorkspaceConfig] = deriveDecoder
    implicit val lDAPConfigDecoder: Decoder[LDAPConfig] = deriveDecoder
    implicit val databaseConfigItemDecoder: Decoder[DatabaseConfigItem] = deriveDecoder
    implicit val databaseConfigDecoder: Decoder[DatabaseConfig] = deriveDecoder
    implicit val kafkaConfigDecoder: Decoder[KafkaConfig] = deriveDecoder
    implicit val generatorConfigDecoder: Decoder[GeneratorConfig] = deriveDecoder
    implicit val appConfigDecoder: Decoder[AppConfig] = deriveDecoder

  }

}
