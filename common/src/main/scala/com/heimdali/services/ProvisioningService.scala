package com.heimdali.services

import cats.data.NonEmptyList
import com.heimdali.models.{Application, KafkaTopic, WorkspaceRequest}
import com.heimdali.provisioning.Message

trait ProvisioningService[F[_]] {

  def provision(workspace: WorkspaceRequest, requiredApprovals: Int = 2): F[NonEmptyList[Message]]

  def findUnprovisioned(): F[List[WorkspaceRequest]]

  def provisionAll(): F[Unit]

  def provisionApplication(workspaceId: Long, application: Application): F[Unit]

  def provisionTopic(workspaceId: Long, topic: KafkaTopic): F[Unit]

}
