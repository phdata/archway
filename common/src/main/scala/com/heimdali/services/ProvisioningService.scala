package com.heimdali.services

import cats.data.NonEmptyList
import com.heimdali.models.WorkspaceRequest
import com.heimdali.provisioning.Message

trait ProvisioningService[F[_]] {

  def provision(workspace: WorkspaceRequest, requiredApprovals: Int = 2): F[NonEmptyList[Message]]

  def findUnprovisioned(): F[List[WorkspaceRequest]]

  def scheduleProvisioning(): F[Unit]

}
