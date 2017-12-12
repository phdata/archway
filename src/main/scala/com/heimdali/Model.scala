package com.heimdali

import java.time.LocalDateTime

import io.circe.generic.extras.ConfiguredJsonCodec
import io.getquill.Embedded

object Model {

  @ConfiguredJsonCodec case class Compliance(phiData: Boolean,
                                             pciData: Boolean,
                                             piiData: Boolean)

  @ConfiguredJsonCodec case class HDFSProvision(location: Option[String],
                                                requestedSizeInGB: Double)

  @ConfiguredJsonCodec case class Project(id: Long,
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