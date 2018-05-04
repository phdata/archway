package com.heimdali.models

import com.typesafe.config.Config
import scalikejdbc._

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
                   initialMembers: Seq[String] = Seq.empty)
  extends Workspace {
  override def workspaceId: String = id.get.toString

  override val databaseName: String = systemName

  override val role: String = s"role_$systemName"

  override def dataDirectory(configuration: Config): String = {
    val baseDirectory = configuration.getString("hdfs.datasetRoot")
    s"$baseDirectory/$name/$systemName"
  }

  override val groupName: String = s"edh_$systemName"

  override val onBehalfOf: Option[String] = None
}

object Dataset extends SQLSyntaxSupport[Dataset] {

  override def tableName: String = "datasets"

  def apply(g: ResultName[Dataset],
            ldapName: ResultName[LDAPRegistration],
            hiveName: ResultName[HiveDatabase],
            yarnName: ResultName[Yarn])
           (resultSet: WrappedResultSet): Dataset =
    Dataset(
      resultSet.longOpt(g.id),
      resultSet.string(g.name),
      resultSet.string(g.systemName),
      resultSet.string(g.purpose),
      resultSet.longOpt(g.ldapRegistrationId),
      LDAPRegistration(ldapName, resultSet),
      resultSet.longOpt(g.hiveDatabaseId),
      HiveDatabase(hiveName, resultSet),
      resultSet.longOpt(g.yarnId),
      Yarn(yarnName, resultSet)
    )

  sealed trait DatasetType {
    def name: String
    def purpose: String
  }
  case object RawDataset extends DatasetType {
    val name = "raw"
    val purpose = "BU or enterprise-wide data sets in raw form"
  }
  case object StagingDataset extends DatasetType {
    val name = "staging"
    val purpose = "ETL space for datasets"
  }
  case object ModeledDataset extends DatasetType {
    val name = "modeled"
    val purpose = "BU or enterprise-wide data sets in modeled or integrated form"
  }

  def apply(datasetType: DatasetType, informationArea: String, createdBy: String): Dataset =
    Dataset(None, datasetType.name, s"${datasetType.name}_$informationArea", datasetType.purpose, initialMembers = Seq(createdBy))

}