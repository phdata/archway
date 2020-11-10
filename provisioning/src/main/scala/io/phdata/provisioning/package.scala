package io.phdata

import java.time.Instant

import cats.effect.{Clock, Sync}
import io.phdata.models.{DatabaseRole, DistinguishedName}

package object provisioning
    extends LDAPRegistrationProvisioning with HiveProvisioning with WorkspaceRequestProvisioning {

  case class WorkspaceContext[F[_]](workspaceId: Long, context: AppContext[F])

  trait Task[A] {
    def run[F[_]: Sync: Clock](a: A, workspaceContext: WorkspaceContext[F]): F[Unit]
  }

  trait ProvisioningTask[A] extends Task[A] with CompletionTask[A]

  trait DeprovisioningTask[A] extends Task[A]

  trait CompletionTask[A] {
    def complete[F[_]: Sync](a: A, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit]
  }

  case class ActiveDirectoryGroup(
      groupId: Long,
      commonName: String,
      distinguishedName: DistinguishedName,
      attributes: List[(String, String)]
  )

  case class DatabaseDirectory(workspaceId: Long, location: String, onBehalfOf: Option[String])

  case class DatabaseGrant(id: Long, roleName: String, databaseName: String, databaseRole: DatabaseRole)

  case class GroupMember(ldapRegistrationId: Long, groupDN: DistinguishedName, distinguishedName: DistinguishedName)

  case class DiskQuota(workspaceId: Long, location: String, sizeInGB: Int)

  case class GroupGrant(ldapId: Long, roleName: String, groupName: String)

  case class HiveDatabaseRegistration(workspaceId: Long, name: String, location: String)

  case class LocationGrant(id: Long, roleName: String, location: String)

  case class SecurityRole(id: Long, name: String)

}
