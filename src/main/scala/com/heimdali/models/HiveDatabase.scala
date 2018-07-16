package com.heimdali.models

import java.time.Instant

import cats._
import cats.effect.Effect
import cats.implicits._
import com.heimdali.repositories.Manager
import io.circe._
import com.heimdali.tasks._

import scala.concurrent.ExecutionContext

case class HiveDatabase(name: String,
                        location: String,
                        sizeInGB: Int,
                        managingGroup: LDAPRegistration,
                        readonlyGroup: Option[LDAPRegistration] = None,
                        id: Option[Long] = None,
                        directoryCreated: Option[Instant] = None,
                        databaseCreated: Option[Instant] = None,
                        quotaSet: Option[Instant] = None,
                        databaseAccessGranted: Option[Instant] = None,
                        locationAccessGranted: Option[Instant] = None)

object HiveDatabase {

  import com.heimdali.tasks.ProvisionResult._
  import com.heimdali.tasks.ProvisionTask._

  implicit val viewer: Show[HiveDatabase] =
    Show.show(h => s"creating hive database ${h.name}")

  implicit def provisioner[F[_] : Effect](implicit executionContext: ExecutionContext): ProvisionTask[F, HiveDatabase] =
    ProvisionTask.instance { hive =>
      for {
        createDirectory <- CreateDatabaseDirectory(hive.id.get, hive.location, None).provision[F]
        setDiskQuota <- SetDiskQuota(hive.id.get, hive.location, hive.sizeInGB).provision[F]
        createDatabase <- CreateHiveDatabase(hive.id.get, hive.name, hive.location).provision[F]
        managers <- hive.managingGroup.provision[F]
        db <- GrantDatabaseAccess(hive.id.get, Manager, hive.managingGroup.sentryRole, hive.name).provision[F]
        location <- GrantLocationAccess(hive.id.get, Manager, hive.managingGroup.sentryRole, hive.location).provision[F]
      } yield createDirectory |+| setDiskQuota |+| createDatabase |+| managers |+| db |+| location
    }

  implicit val encoder: Encoder[HiveDatabase] =
    Encoder.forProduct5("name", "location", "size_in_gb", "managing_group", "readonly_group")(s => (s.name, s.location, s.sizeInGB, s.managingGroup, s.readonlyGroup))

  implicit final val decoder: Decoder[HiveDatabase] =
    Decoder.forProduct5("name", "location", "size_in_gb", "managing_group", "readonly_group")((name: String, location: String, size: Int, managing: LDAPRegistration, readonly: Option[LDAPRegistration]) => HiveDatabase(name, location, size, managing, readonly))

}
