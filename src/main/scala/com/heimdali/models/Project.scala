package com.heimdali.models

import java.time.LocalDateTime

import io.getquill.Embedded

case class Project(id: Long,
                   name: String,
                   purpose: String,
                   ldapDn: Option[String],
                   systemName: String,
                   compliance: Compliance,
                   hdfs: HDFSProvision,
                   keytabLocation: Option[String],
                   created: LocalDateTime,
                   createdBy: String)

object Project {
  def apply(name: String,
            purpose: String,
            compliance: Compliance,
            hdfs: HDFSProvision,
            createdBy: String): Project =
    new Project(123, name, purpose, None, generateName(name), compliance, hdfs, None, LocalDateTime.now(), createdBy)

  def generateName(name: String) =
    name
      .replaceAll("""[^a-zA-Z\d\s]""", " ") //replace with space to remove multiple underscores
      .trim //if we have leading or trailing spaces, clean them up
      .replaceAll("""\s+""", "_")
      .toLowerCase
}

case class Compliance(phiData: Boolean,
                      pciData: Boolean,
                      piiData: Boolean) extends Embedded

case class HDFSProvision(location: Option[String],
                         requestedSizeInGB: Double) extends Embedded
