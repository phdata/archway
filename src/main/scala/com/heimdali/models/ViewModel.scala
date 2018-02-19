package com.heimdali.models

import java.time.LocalDateTime

import com.heimdali.repositories.SharedWorkspaceRecord

object ViewModel {

  case class Compliance(phiData: Boolean,
                        pciData: Boolean,
                        piiData: Boolean)

  case class HDFSProvision(location: Option[String],
                           requestedSizeInGB: Double,
                           actualGB: Option[Double])

  case class YarnProvision(poolName: Option[String],
                           maxCores: Int,
                           maxMemoryInGB: Double)

  case class SharedWorkspaceRequest(name: String,
                                    purpose: String,
                                    compliance: Compliance,
                                    hdfs: HDFSProvision,
                                    yarn: YarnProvision,
                                    createdBy: Option[String])

  case class HiveRequest(requestedSizeInGB: Double)

  case class SharedWorkspace(id: Long,
                             name: String,
                             purpose: String,
                             ldapDn: Option[String],
                             systemName: String,
                             compliance: Compliance,
                             hdfs: HDFSProvision,
                             yarn: YarnProvision,
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
        HDFSProvision(sharedWorkspaceRecord.hdfsLocation, sharedWorkspaceRecord.hdfsRequestedSizeInGb, sharedWorkspaceRecord.hdfsActualSizeInGb),
        YarnProvision(sharedWorkspaceRecord.yarnPoolName, sharedWorkspaceRecord.yarnMaxCores, sharedWorkspaceRecord.yarnMaxMemoryInGb),
        sharedWorkspaceRecord.keytabLocation,
        sharedWorkspaceRecord.created,
        sharedWorkspaceRecord.createdBy)
  }


}