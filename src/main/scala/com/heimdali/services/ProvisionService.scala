package com.heimdali.services

import com.heimdali.models.WorkspaceRequest

trait ProvisionService[F[_]] {

  def provision(workspaceRequest: WorkspaceRequest): F[Unit]

}
