package com.heimdali

import java.net.URLEncoder

import cats.effect.{Async, ContextShift, Resource}
import doobie._
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Strategy
import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, _}

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

  case class ClusterConfig(sessionRefresh: FiniteDuration,
                           url: String,
                           name: String,
                           nameservice: String,
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

    val mgmtServiceUrl = s"$url/api/v18/cm/service"

    val mgmtRoleListUrl = s"$mgmtServiceUrl/roles"

    def mgmtRoleConfigGroups(roleConfigGroupName: String) =
      s"$mgmtServiceUrl/roleConfigGroups/$roleConfigGroupName/config?view=full"

    val refreshUrl = s"$clusterUrl/commands/poolsRefresh"
  }

  object ClusterConfig {

    import io.circe.generic.semiauto._

    implicit val credentialsConfigDecoder: Decoder[CredentialsConfig] = deriveDecoder
    implicit val serviceOverrideDecoder: Decoder[ServiceOverride] = deriveDecoder

    implicit val decoder: Decoder[ClusterConfig] = Decoder.instance { cursor =>
      for {
        sessionRefresh <- cursor.downField("sessionRefresh").as[String].map(Duration.apply(_).asInstanceOf[FiniteDuration])
        url <- cursor.downField("url").as[String]
        name <- cursor.downField("name").as[String]
        nameservice <- cursor.downField("nameservice").as[String]
        environment <- cursor.downField("environment").as[String]
        beeswaxPort <- cursor.downField("beeswaxPort").as[Int]
        hiveServer2Port <- cursor.downField("hiveServer2Port").as[Int]
        admin <- cursor.downField("admin").as[CredentialsConfig]
        hueOverride <- cursor.downField("hueOverride").as[ServiceOverride]
      } yield ClusterConfig(sessionRefresh, url, name, nameservice, environment, beeswaxPort, hiveServer2Port, admin, hueOverride)
    }

    implicit val credentialsConfigEncoder: Encoder[CredentialsConfig] = deriveEncoder
    implicit val serviceOverrideEncoder: Encoder[ServiceOverride] = deriveEncoder

    implicit val encoder: Encoder[ClusterConfig] = new Encoder[ClusterConfig] {
      override def apply(a: ClusterConfig): Json = Json.obj(
        ("sessionRefresh", a.sessionRefresh.toString().asJson),
        ("url", a.url.asJson),
        ("name", a.name.asJson),
        ("environment", a.environment.asJson),
        ("beeswaxPort", a.beeswaxPort.asJson),
        ("hiveServer2Port", a.hiveServer2Port.asJson),
        ("admin", a.admin.asJson),
        ("hueOverride", a.hueOverride.asJson)
      )
    }

  }

  case class RestConfig(port: Int,
                        secret: String,
                        authType: String,
                        principal: String,
                        httpPrincipal: String,
                        keytab: String,
                        sslStore: Option[String] = None,
                        sslStorePassword: Option[String] = None,
                        sslKeyManagerPassword: Option[String] = None)

  case class UIConfig(url: String, staticContentDir: String)

  object UIConfig {

    private def handleDefault(confParam: String) =
      if (confParam.isEmpty) {sys.env("HEIMDALI_UI_HOME")} else confParam

    implicit val decoder: Decoder[UIConfig] = Decoder.instance { cursor =>
      for {
        url <- cursor.downField("url").as[String]
        staticContentDir <- cursor.downField("staticContentDir").as[String]
      } yield UIConfig(url, handleDefault(staticContentDir))
    }
  }

  case class SMTPConfig(fromEmail: String, host: String, port: Int, auth: Boolean, user: Option[String], pass: Option[String], ssl: Boolean)

  case class ApprovalConfig(notificationEmail: String, infrastructure: Option[String], risk: Option[String]) {
    val required: Int = List(infrastructure, risk).flatten.length
  }

  case class WorkspaceConfigItem(root: String, defaultSize: Int, defaultCores: Int, defaultMemory: Int, poolParents: String)

  case class WorkspaceConfig(user: WorkspaceConfigItem, sharedWorkspace: WorkspaceConfigItem, dataset: WorkspaceConfigItem)

  case class LDAPBinding(server: String, port: Int, bindDN: String, bindPassword: String)

  case class LDAPConfig(lookupBinding: LDAPBinding,
                        provisioningBinding: LDAPBinding,
                        filterTemplate: String,
                        memberDisplayTemplate: String,
                        baseDN: String,
                        groupPath: String,
                        userPath: Option[String],
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

  case class ProvisioningConfig(threadPoolSize: Int, provisionInterval: FiniteDuration)

  object ProvisioningConfig {

    implicit val decoder: Decoder[ProvisioningConfig] = Decoder.instance { cursor =>
      for {
        provisionInterval <- cursor.downField("provisionInterval").as[String]
          .map(d => Duration.apply(d).asInstanceOf[FiniteDuration])
        threadpoolSize <- cursor.downField("threadPoolSize").as[Int]
      } yield ProvisioningConfig(threadpoolSize, provisionInterval)
    }

    implicit val encoder: Encoder[ProvisioningConfig] = new Encoder[ProvisioningConfig] {
      override final def apply(a: ProvisioningConfig): Json = Json.obj(
        ("threadPoolSize", a.threadPoolSize.asJson),
        ("provisionInterval", Json.fromString(a.provisionInterval.toString))
      )
    }
  }

  case class DatabaseConfig(meta: DatabaseConfigItem, hive: DatabaseConfigItem)

  case class KafkaConfig(zookeeperConnect: String,
                         secureTopics: Boolean)

  case class TemplateConfig(templateRoot: String,
                            topicGenerator: String,
                            applicationGenerator: String,
                            ldapGroupGenerator: String)
  
  case class AppConfig(templates: TemplateConfig,
                       rest: RestConfig,
                       ui: UIConfig,
                       smtp: SMTPConfig,
                       cluster: ClusterConfig,
                       approvers: ApprovalConfig,
                       ldap: LDAPConfig,
                       db: DatabaseConfig,
                       workspaces: WorkspaceConfig,
                       kafka: KafkaConfig,
                       provisioning: ProvisioningConfig,
                       featureFlags: String
                      )

  object AppConfig {

    import io.circe.generic.semiauto._

    implicit val restConfigDecoder: Decoder[RestConfig] = deriveDecoder
    implicit val sMTPConfigDecoder: Decoder[SMTPConfig] = deriveDecoder
    implicit val approvalConfigDecoder: Decoder[ApprovalConfig] = deriveDecoder
    implicit val workspaceConfigItemDecoder: Decoder[WorkspaceConfigItem] = deriveDecoder
    implicit val workspaceConfigDecoder: Decoder[WorkspaceConfig] = deriveDecoder
    implicit val ldapBindingDecoder: Decoder[LDAPBinding] = deriveDecoder
    implicit val lDAPConfigDecoder: Decoder[LDAPConfig] = deriveDecoder
    implicit val databaseConfigItemDecoder: Decoder[DatabaseConfigItem] = deriveDecoder
    implicit val databaseConfigDecoder: Decoder[DatabaseConfig] = deriveDecoder
    implicit val kafkaConfigDecoder: Decoder[KafkaConfig] = deriveDecoder
    implicit val generatorConfigDecoder: Decoder[TemplateConfig] = deriveDecoder
    implicit val provisioningConfigDecoder: Decoder[ProvisioningConfig] = ProvisioningConfig.decoder
    implicit val appConfigDecoder: Decoder[AppConfig] = deriveDecoder

    implicit val restConfigEncoder: Encoder[RestConfig] = deriveEncoder
    implicit val uIConfigEncoder: Encoder[UIConfig] = deriveEncoder
    implicit val sMTPConfigEncoder: Encoder[SMTPConfig] = deriveEncoder
    implicit val approvalConfigEncoder: Encoder[ApprovalConfig] = deriveEncoder
    implicit val workspaceConfigItemEncoder: Encoder[WorkspaceConfigItem] = deriveEncoder
    implicit val workspaceConfigEncoder: Encoder[WorkspaceConfig] = deriveEncoder
    implicit val ldapBindingEncoder: Encoder[LDAPBinding] = deriveEncoder
    implicit val lDAPConfigEncoder: Encoder[LDAPConfig] = deriveEncoder
    implicit val databaseConfigItemEncoder: Encoder[DatabaseConfigItem] = deriveEncoder
    implicit val databaseConfigEncoder: Encoder[DatabaseConfig] = deriveEncoder
    implicit val kafkaConfigEncoder: Encoder[KafkaConfig] = deriveEncoder
    implicit val generatorConfigEncoder: Encoder[TemplateConfig] = deriveEncoder
    implicit val provisioningConfigEncoder: Encoder[ProvisioningConfig] = ProvisioningConfig.encoder
    implicit val appConfigEncoder: Encoder[AppConfig] = deriveEncoder
  }

}
