package com.heimdali.models

import java.time.LocalDateTime

import io.circe.generic.extras.ConfiguredJsonCodec

object ViewModel {

  case class Compliance(phiData: Boolean,
                        pciData: Boolean,
                        piiData: Boolean)

  case class HDFSProvision(location: Option[String],
                           requestedSizeInGB: Double)

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

}