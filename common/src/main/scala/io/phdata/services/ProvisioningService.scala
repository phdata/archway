package io.phdata.services

import cats.data.NonEmptyList
import cats.effect.Fiber
import io.phdata.models.{WorkspaceRequest}
import io.phdata.provisioning.Message

trait ProvisioningService[F[_]] {

  def attemptProvision(workspace: WorkspaceRequest, requiredApprovals: Int): F[Fiber[F, NonEmptyList[Message]]]

  def attemptDeprovision(workspace: WorkspaceRequest): F[Fiber[F, NonEmptyList[Message]]]

  def findUnprovisioned(): F[List[WorkspaceRequest]]

  def provisionAll(): F[Unit]
}
