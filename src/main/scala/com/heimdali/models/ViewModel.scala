package com.heimdali.models

import java.time.LocalDateTime

import com.heimdali.repositories.SharedWorkspaceRecord
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}

object ViewModel {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  @ConfiguredJsonCodec case class Compliance(phiData: Boolean,
                                             pciData: Boolean,
                                             piiData: Boolean)

  @ConfiguredJsonCodec case class HDFSProvision(location: Option[String],
                           requestedSizeInGB: Double)

  @ConfiguredJsonCodec case class SharedWorkspace(id: Option[Long],
                             name: String,
                             purpose: String,
                             ldapDn: Option[String],
                             systemName: Option[String],
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
        Some(sharedWorkspaceRecord.id),
        sharedWorkspaceRecord.name,
        sharedWorkspaceRecord.purpose,
        sharedWorkspaceRecord.ldapDn,
        Some(sharedWorkspaceRecord.systemName),
        Compliance(sharedWorkspaceRecord.piiData, sharedWorkspaceRecord.pciData, sharedWorkspaceRecord.piiData),
        HDFSProvision(sharedWorkspaceRecord.hdfsLocation, sharedWorkspaceRecord.hdfsRequestedSizeInGb),
        sharedWorkspaceRecord.keytabLocation,
        sharedWorkspaceRecord.created,
        sharedWorkspaceRecord.createdBy)
  }

}