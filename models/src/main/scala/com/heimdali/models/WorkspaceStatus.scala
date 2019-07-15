package com.heimdali.models

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class WorkspaceStatus(provisioning: String)

object WorkspaceProvisioningStatus {
  val COMPLETED = "completed"
  val PENDING = "pending"
  implicit val restConfigEncoder: Encoder[WorkspaceStatus] = deriveEncoder
}
