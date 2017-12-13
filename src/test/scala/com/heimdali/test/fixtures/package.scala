package com.heimdali.test

import java.time.LocalDateTime

import com.heimdali.models.ViewModel._

package object fixtures {

  val standardUsername = "john.doe"

  object TestProject {
    val id = Some(123L)
    val name = "Sesame"
    val purpose = "World Peace"
    val ldapDn: Option[String] = None
    val phiCompliance = false
    val piiCompliance = false
    val pciCompliance = false
    val compliance = Compliance(phiCompliance, piiCompliance, pciCompliance)
    val hdfsLocation: Option[String] = None
    val hdfsRequestedSize = 10
    val hdfs = HDFSProvision(hdfsLocation, hdfsRequestedSize)
    val keytabLocation: Option[String] = None
    val systemName: Option[String] = Some(SharedWorkspace.generateName(name))

    def apply(id: Option[Long] = TestProject.id,
              name: String = TestProject.name,
              purpose: String = TestProject.purpose,
              ldapDn: Option[String] = TestProject.ldapDn,
              systemName: Option[String] = TestProject.systemName,
              compliance: Compliance = TestProject.compliance,
              hdfs: HDFSProvision = TestProject.hdfs,
              keytabLocation: Option[String] = TestProject.keytabLocation,
              createdDate: LocalDateTime = LocalDateTime.now(),
              createdBy: String = standardUsername): SharedWorkspace =
      SharedWorkspace(id, name, purpose, ldapDn, systemName, compliance, hdfs, keytabLocation, createdDate, createdBy)
  }

}
