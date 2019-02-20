package com.heimdali.provisioning

import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.models.WorkspaceRequest
import com.heimdali.services.ProvisioningService

class DefaultProvisioningService[F[_]](appContext: AppContext[F])
                                      (implicit F: Effect[F])
  extends ProvisioningService[F] {

  override def provision(workspace: WorkspaceRequest): F[NonEmptyList[String]] = {
    import com.heimdali.provisioning.ProvisionTask._

    val combined: List[ReaderT[F, AppContext[F], ProvisionResult]] =
      for {
        datas <- workspace.data.map(_.provision)
        dbLiasion <- workspace.data.map(d => AddMember(d.id.get, d.managingGroup.ldapRegistration.distinguishedName, workspace.requestedBy).provision)
        yarns <- if (workspace.processing.isEmpty) List(Kleisli[F, AppContext[F], ProvisionResult](_ => F.pure(NoOp("resource pool")))) else workspace.processing.map(_.provision)
        apps <- if (workspace.applications.isEmpty) List(Kleisli[F, AppContext[F], ProvisionResult](_ => F.pure(NoOp("application")))) else workspace.applications.map(_.provision)
        appLiasion <- if (workspace.applications.isEmpty) List(Kleisli[F, AppContext[F], ProvisionResult](_ => F.pure(NoOp("application liasion")))) else workspace.applications.map(d => AddMember(d.id.get, d.group.distinguishedName, workspace.requestedBy).provision)
      } yield (datas, dbLiasion, yarns, apps, appLiasion).mapN(_ |+| _ |+| _ |+| _ |+| _)

    combined.sequence.map(_.combineAll).apply(appContext).map(_.messages)
  }

}
