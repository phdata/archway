package com.heimdali.services

import cats.data.NonEmptyList
import com.heimdali.models.WorkspaceRequest
import com.heimdali.provisioning.Message

trait ProvisioningService[F[_]] {

  def provision(workspace: WorkspaceRequest): F[NonEmptyList[Message]]

}
