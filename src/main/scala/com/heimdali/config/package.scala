package com.heimdali

import java.net.URLEncoder

import scala.concurrent.duration.FiniteDuration

package object config {

  case class CredentialsConfig(username: String, password: String)

  case class ClusterConfig(sessionRefresh: FiniteDuration,
                           url: String,
                           name: String,
                           environment: String,
                           admin: CredentialsConfig) {
    private val encodedName: String = URLEncoder.encode(name, "utf-8").replaceAll("\\+", "%20")

    val clusterUrl: String = s"$url/clusters/$encodedName"

    def hostUrl(hostId: String) = s"$url/hosts/$hostId"

    val serviceListUrl = s"$clusterUrl/services"

    def serviceUrl(service: String) = s"$serviceListUrl/$service"

    def serviceConfigUrl(service: String) = s"${serviceUrl(service)}/config"

    def serviceRoleListUrl(service: String) = s"${serviceUrl(service)}/roles"

    def serviceRoleUrl(service: String, roleId: String) = s"${serviceRoleListUrl(service)}/$roleId"

    val refreshUrl = s"$clusterUrl/commands/poolsRefresh"
  }

  case class RestConfig(port: Int, secret: String)

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

  case class AppConfig(rest: RestConfig,
                       cluster: ClusterConfig,
                       approvers: ApprovalConfig,
                       ldap: LDAPConfig,
                       db: DatabaseConfig,
                       workspaces: WorkspaceConfig)

}
