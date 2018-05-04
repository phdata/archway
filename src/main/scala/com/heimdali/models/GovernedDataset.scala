package com.heimdali.models

import org.joda.time.DateTime
import scalikejdbc._

case class GovernedDataset(id: Option[Long] = None,
                           name: String,
                           systemName: String,
                           purpose: String,
                           created: DateTime,
                           createdBy: Option[String],
                           requestedSize: Int,
                           requestedCores: Int,
                           requestedMemory: Int,
                           complianceId: Option[Long] = None,
                           compliance: Option[Compliance] = None,
                           rawDatasetId: Option[Long] = None,
                           rawDataset: Option[Dataset] = None,
                           stagingDatasetId: Option[Long] = None,
                           stagingDataset: Option[Dataset] = None,
                           modeledDatasetId: Option[Long] = None,
                           modeledDataset: Option[Dataset] = None)

object GovernedDataset extends SQLSyntaxSupport[GovernedDataset] {

  override def tableName: String = "governed_datasets"

  def apply(g: ResultName[GovernedDataset],
            complianceName: ResultName[Compliance],
            rd: ResultName[Dataset],
            rdLdapName: ResultName[LDAPRegistration],
            rdHiveName: ResultName[HiveDatabase],
            rdYarnName: ResultName[Yarn],
            sd: ResultName[Dataset],
            sdLdapName: ResultName[LDAPRegistration],
            sdHiveName: ResultName[HiveDatabase],
            sdYarnName: ResultName[Yarn],
            md: ResultName[Dataset],
            mdLdapName: ResultName[LDAPRegistration],
            mdHiveName: ResultName[HiveDatabase],
            mdYarnName: ResultName[Yarn])
           (resultSet: WrappedResultSet): GovernedDataset =
    GovernedDataset(
      resultSet.longOpt(g.id),
      resultSet.string(g.name),
      resultSet.string(g.systemName),
      resultSet.string(g.purpose),
      resultSet.jodaDateTime(g.created),
      resultSet.stringOpt(g.createdBy),
      resultSet.int(g.requestedSize),
      resultSet.int(g.requestedCores),
      resultSet.int(g.requestedMemory),
      resultSet.longOpt(g.complianceId),
      Compliance(complianceName, resultSet),
      resultSet.longOpt(g.rawDatasetId),
      resultSet.longOpt(g.rawDatasetId).map(_ => Dataset(rd, rdLdapName, rdHiveName, rdYarnName)(resultSet)),
      resultSet.longOpt(g.stagingDatasetId),
      resultSet.longOpt(g.stagingDatasetId).map(_ => Dataset(sd, sdLdapName, sdHiveName, sdYarnName)(resultSet)),
      resultSet.longOpt(g.modeledDatasetId),
      resultSet.longOpt(g.modeledDatasetId).map(_ => Dataset(md, mdLdapName, mdHiveName, mdYarnName)(resultSet))
    )
}