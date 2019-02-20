package com.heimdali.services

import cats.data.NonEmptyList
import com.heimdali.models.WorkspaceRequest

trait ProvisioningService[F[_]] {

  def provision(workspace: WorkspaceRequest): F[NonEmptyList[String]]

}
