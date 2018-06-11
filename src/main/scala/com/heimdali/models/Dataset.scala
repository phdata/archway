package com.heimdali.models

import com.typesafe.config.Config

case class Dataset(id: Option[Long] = None,
                   name: String,
                   systemName: String,
                   purpose: String,
                   ldapRegistrationId: Option[Long] = None,
                   ldap: Option[LDAPRegistration] = None,
                   hiveDatabaseId: Option[Long] = None,
                   hiveDatabase: Option[HiveDatabase] = None,
                   yarnId: Option[Long] = None,
                   yarn: Option[Yarn] = None,
                   initialMembers: Seq[String] = Seq.empty) {
  def configName: String = "dataset"

  def poolName: String = systemName

  lazy val workspaceId: Long = id.get

  val databaseName: String = s"${name}_${systemName}"

  def role(configuration: Config): String = {
    val environment = configuration.getString("cluster.environment")
    s"role_${environment}_${name}_$systemName"
  }

  def dataDirectory(configuration: Config): String = {
    val baseDirectory = configuration.getString(s"workspaces.$configName.root")
    s"$baseDirectory/$name/$systemName"
  }

  def groupName(configuration: Config): String = {
    val environment = configuration.getString("cluster.environment")
    s"edh_${environment}_${name}_$systemName"
  }

  val onBehalfOf: Option[String] = None
}