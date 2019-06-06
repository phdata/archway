package com.heimdali.provisioning

import cats.Show
import cats.data.{NonEmptyList, WriterT}
import cats.effect.{Clock, Sync}
import cats.implicits._
import com.heimdali.models.{HiveAllocation, HiveGrant}
import com.heimdali.provisioning.Provisionable.ops._

trait HiveProvisioning {

  implicit object HiveGrantProvisionable extends Provisionable[HiveGrant] {
    override def provision[F[_] : Clock : Sync](hiveGrant: HiveGrant, workspaceContext: WorkspaceContext[F])(implicit show: Show[HiveGrant]): WriterT[F, NonEmptyList[Message], ProvisionResult] =
      for {
        ldap <- hiveGrant.ldapRegistration.provision[F](workspaceContext)
        db <- GrantDatabaseAccess(hiveGrant.id.get, hiveGrant.ldapRegistration.sentryRole, hiveGrant.databaseName, hiveGrant.databaseRole).provision[F](workspaceContext)
        location <- GrantLocationAccess(hiveGrant.id.get, hiveGrant.ldapRegistration.sentryRole, hiveGrant.location).provision[F](workspaceContext)
      } yield ldap |+| db |+| location
  }

  implicit object HiveAllocationProvisionable extends Provisionable[HiveAllocation] {

    override def provision[F[_] : Clock : Sync](hiveAllocation: HiveAllocation, workspaceContext: WorkspaceContext[F])(implicit show: Show[HiveAllocation]): WriterT[F, NonEmptyList[Message], ProvisionResult] =
      for {
        createDirectory <- CreateDatabaseDirectory(hiveAllocation.id.get, hiveAllocation.location, None).provision[F](workspaceContext)
        setDiskQuota <- SetDiskQuota(hiveAllocation.id.get, hiveAllocation.location, hiveAllocation.sizeInGB).provision[F](workspaceContext)
        createDatabase <- CreateHiveDatabase(hiveAllocation.id.get, hiveAllocation.name, hiveAllocation.location).provision[F](workspaceContext)
        managers <- hiveAllocation.managingGroup.provision[F](workspaceContext)
        readwrite <- hiveAllocation.readWriteGroup.map(_.provision[F](workspaceContext))
          .getOrElse(WriterT((NoOp.message(workspaceContext.workspaceId, "read/write group"), NoOp).pure[F]))
        readonly <- hiveAllocation.readonlyGroup.map(_.provision[F](workspaceContext))
          .getOrElse(WriterT((NoOp.message(workspaceContext.workspaceId, "read only group"), NoOp).pure[F]))
      } yield createDirectory |+| setDiskQuota |+| createDatabase |+| managers |+| readwrite |+| readonly
  }

}
