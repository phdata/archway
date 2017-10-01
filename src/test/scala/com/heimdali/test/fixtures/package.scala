package com.heimdali.test

import java.time.LocalDateTime

import com.heimdali.models.{Compliance, Project}

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
    val systemName = Project(1, name, purpose, ldapDn, "", compliance, LocalDateTime.now, standardUsername).generatedName

    def apply(id: Long = TestProject.id,
              name: String = TestProject.name,
              purpose: String = TestProject.purpose,
              ldapDn: Option[String] = TestProject.ldapDn,
              systemName: String = TestProject.systemName,
              compliance: Compliance = TestProject.compliance,
              createdDate: LocalDateTime = LocalDateTime.now(),
              createdBy: String = standardUsername): Project =
      Project(id, name, purpose, ldapDn, systemName, compliance, createdDate, createdBy)
  }

}
