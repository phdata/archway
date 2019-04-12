package com.heimdali.provisioning

import java.time.Instant

import cats.Apply
import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.models.WorkspaceRequest
import com.heimdali.services.ProvisioningService
import doobie.implicits._
import com.heimdali.provisioning.ProvisionTask._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

class DefaultProvisioningService[F[_] : ContextShift: ConcurrentEffect : Effect : Timer](appContext: AppContext[F], provisionContext: ExecutionContext)
  extends ProvisioningService[F] {

  override def provisionAll(): F[Unit] =
    for {
      workspaces <- findUnprovisioned()
      _ <- ContextShift[F].evalOn(provisionContext)(workspaces.traverse(ws => provision(ws)))
    } yield ()

  private def provisionSteps(workspace: WorkspaceRequest): List[ReaderT[F, WorkspaceContext[F], ProvisionResult]] =
    for {
      datas <- workspace.data.map(_.provision)
      dbLiasion <- workspace.data.map(d => AddMember(d.id.get, d.managingGroup.ldapRegistration.distinguishedName, workspace.requestedBy).provision)
      yarns <- if (workspace.processing.isEmpty) List(Kleisli[F, WorkspaceContext[F], ProvisionResult](_ => Effect[F].pure(NoOp("resource pool")))) else workspace.processing.map(_.provision)
      apps <- if (workspace.applications.isEmpty) List(Kleisli[F, WorkspaceContext[F], ProvisionResult](_ => Effect[F].pure(NoOp("application")))) else workspace.applications.map(_.provision)
      appLiasion <- if (workspace.applications.isEmpty) List(Kleisli[F, WorkspaceContext[F], ProvisionResult](_ => Effect[F].pure(NoOp("application liasion")))) else workspace.applications.map(d => AddMember(d.id.get, d.group.distinguishedName, workspace.requestedBy).provision)
      topics <- if (workspace.kafkaTopics.isEmpty) List(Kleisli[F, WorkspaceContext[F], ProvisionResult](_ => Effect[F].pure(NoOp("kafka topic")))) else workspace.kafkaTopics.map(_.provision)
      topicLiaison <- if (workspace.kafkaTopics.isEmpty) List(Kleisli[F, WorkspaceContext[F], ProvisionResult](_ => Effect[F].pure(NoOp("application liaison")))) else workspace.kafkaTopics.map(d => AddMember(d.id.get, d.managingRole.ldapRegistration.distinguishedName, workspace.requestedBy).provision)
    } yield (datas, dbLiasion, yarns, apps, appLiasion, topics, topicLiaison).mapN(_ |+| _ |+| _ |+| _ |+| _ |+| _ |+| _)

  private def markProvisioned(workspaceId: Long, time: Instant): F[Int] =
    appContext
      .workspaceRequestRepository
      .markProvisioned(workspaceId, time)
      .transact(appContext.transactor)

  private def runProvisioning(workspace: WorkspaceRequest) =
    for {
      time <- Timer[F].clock.realTime(MILLISECONDS)
      workspaceContext = (workspace.id, appContext)
      provisionResult <- provisionSteps(workspace).sequence.map(_.combineAll).apply(workspaceContext)
      messages = provisionResult.messages
      instant = Instant.ofEpochMilli(time)
      workspaceId = workspace.id.get
      update = markProvisioned(workspaceId, instant)
      messagesF = messages.pure[F]
      result <- if(provisionResult.isInstanceOf[Success]) Apply[F].productL(messagesF)(update) else messagesF
    } yield result

  override def provision(workspace: WorkspaceRequest, requiredApprovals: Int = 2): F[NonEmptyList[Message]] =
    if (workspace.approvals.lengthCompare(requiredApprovals) == 0) {
      ContextShift[F].evalOn(provisionContext)(runProvisioning(workspace))
    } else {
      NonEmptyList.one[Message](SimpleMessage(workspace.id, s"Skipping workspace build. Workspace has ${workspace.approvals.length} but requires $requiredApprovals")).pure[F]
    }

  override def findUnprovisioned(): F[List[WorkspaceRequest]] = {
    appContext.workspaceRequestRepository.findUnprovisioned().transact(appContext.transactor)
  }
}