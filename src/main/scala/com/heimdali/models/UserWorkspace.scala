package com.heimdali.models

import com.typesafe.config.Config
import scalikejdbc._

case class UserWorkspace(username: String,
                         ldapRegistrationId: Option[Long] = None,
                         ldap: Option[LDAPRegistration] = None,
                         hiveDatabaseId: Option[Long] = None,
                         hiveDatabase: Option[HiveDatabase] = None,
                         yarnId: Option[Long] = None,
                         yarn: Option[Yarn] = None) extends Workspace {
  override def workspaceId: String = username

  override val databaseName: String = s"user_$username"

  override def role(configuration: Config): String = s"role_$username"

  override def dataDirectory(configuration: Config): String = {
    val userDirectory = configuration.getString("hdfs.userRoot")
    s"$userDirectory/$username/db"
  }

  override def groupName(configuration: Config): String = s"edh_user_$username"

  override val initialMembers: Seq[String] = Seq(username)

  override val onBehalfOf: Option[String] = Some(username)
}

object UserWorkspace extends SQLSyntaxSupport[UserWorkspace] {

  override def tableName: String = "user_workspace"

  def apply(userWorkspaceName: ResultName[UserWorkspace],
            ldapName: ResultName[LDAPRegistration],
            hiveName: ResultName[HiveDatabase],
            yarnName: ResultName[Yarn])
           (resultSet: WrappedResultSet): UserWorkspace =
    UserWorkspace(
      resultSet.string(userWorkspaceName.username),
      resultSet.longOpt(userWorkspaceName.ldapRegistrationId),
      LDAPRegistration(ldapName, resultSet),
      resultSet.longOpt(userWorkspaceName.hiveDatabaseId),
      HiveDatabase(hiveName, resultSet),
      resultSet.longOpt(userWorkspaceName.yarnId),
      Yarn(yarnName, resultSet)
    )
}