package com.heimdali.models

import cats._
import cats.effect.Effect
import cats.implicits._
import io.circe._

import scala.concurrent.ExecutionContext

case class HiveDatabase(name: String,
                        location: String,
                        sizeInGB: Int,
                        managingGroup: LDAPRegistration,
                        readonlyGroup: Option[LDAPRegistration] = None,
                        id: Option[Long] = None)

object HiveDatabase {

  import com.heimdali.tasks.ProvisionResult._
  import com.heimdali.tasks.ProvisionTask._
  import com.heimdali.tasks._

  implicit val viewer: Show[HiveDatabase] =
    Show.show(h => s"creating hive database ${h.name}")

  implicit def provisioner[F[_] : Effect](implicit executionContext: ExecutionContext): ProvisionTask[F, HiveDatabase] =
    ProvisionTask.instance { hive =>
      for {
        createDirectory <- CreateDatabaseDirectory(hive.location, None).provision[F]
        setDiskQuota <- SetDiskQuota(hive.location, hive.sizeInGB).provision[F]
        createDatabase <- CreateHiveDatabase(hive.name, hive.location).provision[F]
        managers <- hive.managingGroup.provision[F]
        group <- GrantGroupAccess(hive.managingGroup.sentryRole, hive.managingGroup.commonName).provision[F]
        db <- GrantDatabaseAccess(hive.managingGroup.sentryRole, hive.name).provision[F]
        location <- GrantLocationAccess(hive.managingGroup.sentryRole, hive.location).provision[F]
      } yield createDirectory |+| setDiskQuota |+| createDatabase |+| managers |+| group |+| db |+| location
    }

  implicit val encoder: Encoder[HiveDatabase] =
    Encoder.forProduct5("name", "location", "size_in_gb", "managing_group", "readonly_group")(s => (s.name, s.location, s.sizeInGB, s.managingGroup, s.readonlyGroup))

  implicit final val decoder: Decoder[HiveDatabase] =
    Decoder.forProduct5("name", "location", "size_in_gb", "managing_group", "readonly_group")((name: String, location: String, size: Int, managing: LDAPRegistration, readonly: Option[LDAPRegistration]) => HiveDatabase(name, location, size, managing, readonly))

}
