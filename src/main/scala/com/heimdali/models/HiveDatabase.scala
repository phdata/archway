package com.heimdali.models

import cats._
import cats.effect.Effect
import cats.implicits._
import com.heimdali.tasks.ProvisionResult._
import com.heimdali.tasks.ProvisionTask._
import com.heimdali.tasks._
import io.circe._

import scala.concurrent.ExecutionContext

case class HiveDatabase(name: String,
                        location: String,
                        sizeInGB: Int,
                        managingGroup: HiveGrant,
                        readonlyGroup: Option[HiveGrant] = None,
                        id: Option[Long] = None)

object HiveDatabase {

  def apply(name: String,
            location: String,
            sizeInGB: Int,
            managerLDAP: LDAPRegistration,
            readonlyLDAP: Option[LDAPRegistration]): HiveDatabase =
    apply(name, location, sizeInGB, HiveGrant(name, location, managerLDAP), readonlyLDAP.map(ldap => HiveGrant(name, location, ldap)))

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
    Decoder.forProduct5("name", "location", "size_in_gb", "managing_group", "readonly_group")((name: String, location: String, size: Int, managing: HiveGrant, readonly: Option[HiveGrant]) =>
      HiveDatabase(name, location, size, managing, readonly))

}
