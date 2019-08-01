package io.phdata.models

import java.time.Instant

import io.circe._
import io.circe.java8.time._
import io.circe.syntax._

case class WorkspaceSearchResult(
    id: Long,
    name: String,
    summary: String,
    behavior: String,
    status: String,
    piiData: Boolean,
    pciData: Boolean,
    phiData: Boolean,
    requested: Instant,
    fullyApproved: Option[Instant],
    diskAllocatedInGB: BigDecimal,
    maxCores: Long,
    maxMemoryInGB: BigDecimal
)

object WorkspaceSearchResult {

  implicit val encoder: Encoder[WorkspaceSearchResult] = Encoder.instance { result =>
    Json.obj(
      "id" -> result.id.asJson,
      "name" -> result.name.asJson,
      "summary" -> result.summary.asJson,
      "behavior" -> result.behavior.asJson,
      "status" -> result.status.asJson,
      "pii_data" -> result.piiData.asJson,
      "pci_data" -> result.pciData.asJson,
      "phi_data" -> result.phiData.asJson,
      "date_requested" -> result.requested.asJson,
      "date_fully_approved" -> result.fullyApproved.asJson,
      "total_disk_allocated_in_gb" -> result.diskAllocatedInGB.asJson,
      "total_max_cores" -> result.maxCores.asJson,
      "total_max_memory_in_gb" -> result.maxMemoryInGB.asJson
    )
  }

}
