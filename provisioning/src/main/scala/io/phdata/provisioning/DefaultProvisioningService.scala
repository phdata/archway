package io.phdata.provisioning

import cats.data._
import cats.effect._
import cats.implicits._
import io.phdata.AppContext
import io.phdata.models.{Application, WorkspaceRequest}
import doobie.implicits._
import io.phdata.services.ProvisioningService

import scala.concurrent.ExecutionContext

class DefaultProvisioningService[F[_]: ContextShift: ConcurrentEffect: Timer](
    appContext: AppContext[F],
    provisionContext: ExecutionContext
) extends ProvisioningService[F] {

  override def provisionAll(): F[Unit] =
    for {
      workspaces <- findUnprovisioned()
      _ <- ContextShift[F].evalOn(provisionContext)(
        workspaces.traverse(ws => attemptProvision(ws, appContext.appConfig.approvers.required))
      )
    } yield ()

  override def attemptProvision(
      workspace: WorkspaceRequest,
      requiredApprovals: Int
  ): F[Fiber[F, NonEmptyList[Message]]] = {
    val provisioning: F[NonEmptyList[Message]] = workspace.approvals match {
      case x if x.length >= requiredApprovals =>
        ContextShift[F].evalOn(provisionContext)(
          workspaceRequestProvisionable
            .provision(workspace, (WorkspaceContext(workspace.id.get, appContext)))
            .run
            .map(_._1)
        )
      case _ =>
        NonEmptyList
          .one[Message](
            SimpleMessage(
              workspace.id.get,
              s"Skipping workspace build. Workspace has ${workspace.approvals.length} but requires $requiredApprovals"
            )
          )
          .pure[F]
    }
    ConcurrentEffect[F].start(provisioning)
  }

  override def attemptDeprovision(workspace: WorkspaceRequest): F[Fiber[F, NonEmptyList[Message]]] = {
    val provisioning: F[NonEmptyList[Message]] =
      ContextShift[F].evalOn(provisionContext)(
        workspaceRequestProvisionable
          .deprovision(workspace, WorkspaceContext(workspace.id.get, appContext))
          .run
          .map(_._1)
      )
    ConcurrentEffect[F].start(provisioning)
  }

  override def findUnprovisioned(): F[List[WorkspaceRequest]] = {
    appContext.workspaceRequestRepository.findUnprovisioned().transact(appContext.transactor)
  }

  override def provisionApplication(workspaceId: Long, application: Application): F[Unit] =
    ApplicationProvisionable.provision(application, WorkspaceContext(workspaceId, appContext)).run.void
}
