package com.heimdali

import java.net.URLEncoder

import scala.concurrent.duration.FiniteDuration

package object config {

  case class CredentialsConfig(username: String, password: String)

  case class ServiceOverride(host: Option[String], port: Option[Int])

  case class ClusterConfig(sessionRefresh: FiniteDuration,
                           url: String,
                           name: String,
                           environment: String,
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

  case class RestConfig(port: Int, secret: String)

  case class UIConfig(url: String)

  case class SMTPConfig(host: String, port: Int, auth: Boolean, user: Option[String], pass: Option[String], ssl: Boolean)

  case class ApprovalConfig(infrastructure: String, risk: String)

  case class WorkspaceConfigItem(root: String, defaultSize: Int, defaultCores: Int, defaultMemory: Int, poolParents: String)

  case class WorkspaceConfig(user: WorkspaceConfigItem, sharedWorkspace: WorkspaceConfigItem, dataset: WorkspaceConfigItem)

  case class LDAPConfig(server: String,
                        port: Int,
                        baseDN: String,
                        groupPath: String,
                        userPath: Option[String],
                        bindDN: String,
                        bindPassword: String,
                        domain: String)

  case class DatabaseConfigItem(driver: String, url: String, username: Option[String], password: Option[String])

  case class DatabaseConfig(meta: DatabaseConfigItem, hive: DatabaseConfigItem)

  case class KafkaConfig(zookeeperConnect: String)

  case class AppConfig(rest: RestConfig,
                       ui: UIConfig,
                       smtp: SMTPConfig,
                       cluster: ClusterConfig,
                       approvers: ApprovalConfig,
                       ldap: LDAPConfig,
                       db: DatabaseConfig,
                       workspaces: WorkspaceConfig,
                       kafka: KafkaConfig)

}
