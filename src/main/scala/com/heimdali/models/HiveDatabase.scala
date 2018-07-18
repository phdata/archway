package com.heimdali.models

import java.time.Instant

import cats._
import cats.data.Kleisli
import cats.effect.Effect
import cats.implicits._
import com.heimdali.repositories.Manager
import io.circe._
import com.heimdali.tasks._

import com.heimdali.tasks.ProvisionResult._
import com.heimdali.tasks.ProvisionTask._

import scala.concurrent.ExecutionContext

case class HiveGrant(databaseName: String,
                     location: String,
                     ldapRegistration: LDAPRegistration,
                     id: Option[Long] = None,
                     locationAccess: Option[Instant] = None,
                     databaseAccess: Option[Instant] = None)

object HiveGrant {

  implicit val show: Show[HiveGrant] =
    Show.show(g => s"granting ${g.ldapRegistration.commonName} access to ${g.databaseName}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, HiveGrant] =
    ProvisionTask.instance { grant =>
      for {
        db <- GrantDatabaseAccess(grant.id.get, grant.ldapRegistration.sentryRole, grant.databaseName).provision[F]
        location <- GrantLocationAccess(grant.id.get, grant.ldapRegistration.sentryRole, grant.location).provision[F]
      } yield db |+| location
    }

  implicit val encoder: Encoder[HiveGrant] = ???

  implicit val decoder: Decoder[HiveGrant] = ???

}

case class HiveDatabase(name: String,
                        location: String,
                        sizeInGB: Int,
                        managingGroup: HiveGrant,
                        readonlyGroup: HiveGrant,
                        workspaceRequestId: Option[Long] = None,
                        id: Option[Long] = None)

object HiveDatabase {

  def apply(name: String,
            location: String,
            sizeInGB: Int,
            managerLDAP: LDAPRegistration,
            readonlyLDAP: LDAPRegistration): HiveDatabase =
    apply(name, location, sizeInGB, HiveGrant(name, location, managerLDAP), HiveGrant(name, location, readonlyLDAP))

  implicit val viewer: Show[HiveDatabase] =
    Show.show(h => s"creating hive database ${h.name}")

  implicit def provisioner[F[_] : Effect](implicit executionContext: ExecutionContext): ProvisionTask[F, HiveDatabase] =
    ProvisionTask.instance { hive =>
      for {
        createDirectory <- CreateDatabaseDirectory(hive.id.get, hive.location, None).provision[F]
        setDiskQuota <- SetDiskQuota(hive.id.get, hive.location, hive.sizeInGB).provision[F]
        createDatabase <- CreateHiveDatabase(hive.id.get, hive.name, hive.location).provision[F]
        managers <- hive.managingGroup.provision[F]
      } yield createDirectory |+| setDiskQuota |+| createDatabase |+| managers
    }

  implicit val encoder: Encoder[HiveDatabase] =
    Encoder.forProduct5("name", "location", "size_in_gb", "managing_group", "readonly_group")(s => (s.name, s.location, s.sizeInGB, s.managingGroup, s.readonlyGroup))

  implicit final val decoder: Decoder[HiveDatabase] =
    Decoder.forProduct5("name", "location", "size_in_gb", "managing_group", "readonly_group")((name: String, location: String, size: Int, managing: HiveGrant, readonly: HiveGrant) =>
      HiveDatabase(name, location, size, managing, readonly))

}
