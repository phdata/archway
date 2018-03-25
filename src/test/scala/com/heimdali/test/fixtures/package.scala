package com.heimdali.test

import java.time.LocalDateTime
import java.util.Date

import com.heimdali.models.ViewModel._

package object fixtures {

  val standardUsername = "john.doe"

  object TestProject {
    val id = 123L
    val name = "Sesame"
    val purpose = "World Peace"
    val ldapDn: Option[String] = None
    val phiCompliance = false
    val piiCompliance = false
    val pciCompliance = false
    val compliance = Compliance(phiCompliance, piiCompliance, pciCompliance)
    val hdfsLocation: Option[String] = None
    val hdfsRequestedSize: Double = 10.0
    val actualGB = None
    val keytabLocation: Option[String] = None
    val systemName: String = SharedWorkspace.generateName(name)
    val maxCores = 1
    val maxMemoryInGB = 1.0
    val poolName = None
    val hdfs = HDFSProvision(hdfsLocation, hdfsRequestedSize, None)
    val yarn = YarnProvision(poolName, maxCores, maxMemoryInGB)

    def apply(id: Long = TestProject.id,
              name: String = TestProject.name,
              purpose: String = TestProject.purpose,
              ldapDn: Option[String] = TestProject.ldapDn,
              systemName: String = TestProject.systemName,
              compliance: Compliance = TestProject.compliance,
              hdfs: HDFSProvision = TestProject.hdfs,
              yarn: YarnProvision = TestProject.yarn,
              keytabLocation: Option[String] = TestProject.keytabLocation,
              createdDate: LocalDateTime = LocalDateTime.now(),
              createdBy: String = standardUsername): SharedWorkspace =
      SharedWorkspace(id, name, purpose, ldapDn, systemName, compliance, hdfs, yarn, keytabLocation, createdDate, createdBy)
  }

}
