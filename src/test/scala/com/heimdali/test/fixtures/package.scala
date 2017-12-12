package com.heimdali.test

import java.time.LocalDateTime

import com.heimdali.models.{Compliance, HDFSProvision, Project}

package object fixtures {

  val standardUsername = "john.doe"

  object TestProject {
    val id = 123L
    val name = "Sesame"
    val purpose = "World Peace"
    val ldapDn = None
    val phiCompliance = false
    val piiCompliance = false
    val pciCompliance = false
    val compliance = Compliance(phiCompliance, piiCompliance, pciCompliance)
    val hdfsLocation = None
    val hdfsRequestedSize = 10
    val hdfs = HDFSProvision(hdfsLocation, hdfsRequestedSize)
    val keytabLocation = None
    val systemName = Project.generateName(name)

    def apply(id: Long = TestProject.id,
              name: String = TestProject.name,
              purpose: String = TestProject.purpose,
              ldapDn: Option[String] = TestProject.ldapDn,
              systemName: String = TestProject.systemName,
              compliance: Compliance = TestProject.compliance,
              hdfs: HDFSProvision = TestProject.hdfs,
              keytabLocation: Option[String] = TestProject.keytabLocation,
              createdDate: LocalDateTime = LocalDateTime.now(),
              createdBy: String = standardUsername): Project =
      Project(id, name, purpose, ldapDn, systemName, compliance, hdfs, keytabLocation, createdDate, createdBy)
  }

}
