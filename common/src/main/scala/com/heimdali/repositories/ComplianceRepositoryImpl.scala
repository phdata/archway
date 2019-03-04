package com.heimdali.repositories

import com.heimdali.models.Compliance
import doobie._
import doobie.implicits._

class ComplianceRepositoryImpl
  extends ComplianceRepository {

  implicit val han = LogHandler.jdkLogHandler

  def insertRecord(compliance: Compliance): ConnectionIO[Long] =
    sql"""
       insert into compliance (phi_data, pci_data, pii_data)
       values(
        ${compliance.phiData},
        ${compliance.pciData},
        ${compliance.piiData}
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

}
