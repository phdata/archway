package io.phdata.repositories

import io.phdata.models.Compliance
import doobie._
import doobie.implicits._

class ComplianceRepositoryImpl extends ComplianceRepository {

  implicit val han = CustomLogHandler.logHandler(this.getClass)

  def insertRecord(compliance: Compliance): ConnectionIO[Long] =
    sql"""
       insert into compliance (phi_data, pci_data, pii_data)
       values(
        ${toChar(compliance.phiData).toString},
        ${toChar(compliance.pciData).toString},
        ${toChar(compliance.piiData).toString}
       )
      """.update.withUniqueGeneratedKeys[Long]("id")

  def find(id: Long): ConnectionIO[Compliance] =
    sql"""
       select
         phi_data,
         pci_data,
         pii_data,
         id
       from
         compliance
       where
         id = $id
      """.query[Compliance].unique

  override def create(compliance: Compliance): ConnectionIO[Compliance] =
    for {
      id <- insertRecord(compliance)
      result <- find(id)
    } yield result

  private def toChar(boolean: Boolean): Char = {
    boolean match {
      case true  => '1'
      case false => '0'
    }
  }

}
