package com.heimdali.models

import java.time.LocalDateTime

import com.heimdali.repositories.SharedWorkspaceRecord

object ViewModel {

  case class Compliance(phiData: Boolean,
                        pciData: Boolean,
                        piiData: Boolean)

  case class HDFSProvision(location: Option[String],
                           requestedSizeInGB: Double)

  case class SharedWorkspaceRequest(name: String,
                                    purpose: String,
                                    compliance: Compliance,
                                    hdfs: HDFSProvision,
                                    createdBy: Option[String])

  case class SharedWorkspace(id: Long,
                             name: String,
                             purpose: String,
                             ldapDn: Option[String],
                             systemName: String,
                             compliance: Compliance,
                             hdfs: HDFSProvision,
                             keytabLocation: Option[String],
                             created: LocalDateTime,
                             createdBy: String)

  object SharedWorkspace {
    def generateName(name: String): String =
      name
        .replaceAll("""[^a-zA-Z\d\s]""", " ") //replace with space to remove multiple underscores
        .trim //if we have leading or trailing spaces, clean them up
        .replaceAll("""\s+""", "_")
        .toLowerCase

    def apply(sharedWorkspaceRecord: SharedWorkspaceRecord): SharedWorkspace =
      SharedWorkspace(
        sharedWorkspaceRecord.id,
        sharedWorkspaceRecord.name,
        sharedWorkspaceRecord.purpose,
        sharedWorkspaceRecord.ldapDn,
        sharedWorkspaceRecord.systemName,
        Compliance(sharedWorkspaceRecord.piiData, sharedWorkspaceRecord.pciData, sharedWorkspaceRecord.piiData),
        HDFSProvision(sharedWorkspaceRecord.hdfsLocation, sharedWorkspaceRecord.hdfsRequestedSizeInGb),
        sharedWorkspaceRecord.keytabLocation,
        sharedWorkspaceRecord.created,
        sharedWorkspaceRecord.createdBy)
  }


}