package com.heimdali.models

import com.typesafe.config.Config
import org.joda.time.DateTime
import scalikejdbc._

case class SharedWorkspace(id: Option[Long] = None,
                           name: String,
                           systemName: String,
                           purpose: String,
                           created: DateTime,
                           createdBy: String,
                           requestedSize: Int,
                           requestedCores: Int,
                           requestedMemory: Int,
                           complianceId: Option[Long] = None,
                           compliance: Option[Compliance] = None,
                           ldapRegistrationId: Option[Long] = None,
                           ldap: Option[LDAPRegistration] = None,
                           hiveDatabaseId: Option[Long] = None,
                           hiveDatabase: Option[HiveDatabase] = None,
                           yarnId: Option[Long] = None,
                           yarn: Option[Yarn] = None)
extends Workspace[Long] {
  override def configName: String = "sharedWorkspace"

  override def poolName: String = s"sw_$systemName"

  override lazy val workspaceId: Long = id.get

  override val databaseName: String = s"sw_$systemName"

  override def role(configuration: Config): String = s"role_sw_$systemName"

  override def dataDirectory(configuration: Config): String = {
    val baseDirectory = configuration.getString("workspaces.sharedWorkspace.root")
    s"$baseDirectory/$systemName"
  }

  override def groupName(configuration: Config): String = {
    s"edh_sw_$systemName"
  }

  override val initialMembers: Seq[String] = Seq(createdBy)

  override val onBehalfOf: Option[String] = None
}

object SharedWorkspace extends SQLSyntaxSupport[SharedWorkspace] {


  override def tableName: String = "shared_workspace"

  def apply(g: ResultName[SharedWorkspace],
            ldapName: ResultName[LDAPRegistration],
            complianceName: ResultName[Compliance],
            hiveName: ResultName[HiveDatabase],
            yarnName: ResultName[Yarn])
           (resultSet: WrappedResultSet): SharedWorkspace =
    SharedWorkspace(
      resultSet.longOpt(g.id),
      resultSet.string(g.name),
      resultSet.string(g.systemName),
      resultSet.string(g.purpose),
      resultSet.jodaDateTime(g.created),
      resultSet.string(g.createdBy),
      resultSet.int(g.requestedSize),
      resultSet.int(g.requestedCores),
      resultSet.int(g.requestedMemory),
      resultSet.longOpt(g.complianceId),
      Compliance(complianceName, resultSet),
      resultSet.longOpt(g.ldapRegistrationId),
      LDAPRegistration(ldapName, resultSet),
      resultSet.longOpt(g.hiveDatabaseId),
      HiveDatabase(hiveName, resultSet),
      resultSet.longOpt(g.yarnId),
      Yarn(yarnName, resultSet)
    )

  def generateName(name: String): String =
    name
      .replaceAll("""[^a-zA-Z\d\s]""", " ") //replace with space to remove multiple underscores
      .trim //if we have leading or trailing spaces, clean them up
      .replaceAll("""\s+""", "_")
      .toLowerCase

}