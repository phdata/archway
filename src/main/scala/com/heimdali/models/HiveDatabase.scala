package com.heimdali.models

import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import io.circe._

case class HiveDatabase(name: String,
                        location: String,
                        sizeInGB: Int,
                        managingGroup: LDAPRegistration,
                        readonlyGroup: Option[LDAPRegistration] = None,
                        id: Option[Long] = None)

object HiveDatabase {

  import com.heimdali.tasks._
  import com.heimdali.tasks.ProvisionTask._

  implicit val viewer: Show[HiveDatabase] = ???

  implicit val provisioner: ProvisionTask[HiveDatabase] =
    hive => for {
      _ <- CreateDatabaseDirectory(hive.location).provision
      _ <- SetDiskQuota(hive.location, hive.sizeInGB).provision
      _ <- hive.managingGroup.provision
      _ <- GrantGroupAccess(hive.managingGroup.sentryRole, hive.managingGroup.commonName).provision
      _ <- GrantDatabaseAccess(hive.managingGroup.sentryRole, hive.name).provision
      _ <- GrantLocationAccess(hive.managingGroup.sentryRole, hive.location).provision
    } yield Success[HiveDatabase]("")

  implicit val encoder: Encoder[HiveDatabase] =
    Encoder.forProduct5("name", "location", "size_in_gb", "managing_group", "readonly_group")(s => (s.name, s.location, s.sizeInGB, s.managingGroup, s.readonlyGroup))

  implicit final val decoder: Decoder[HiveDatabase] =
    Decoder.forProduct5("name", "location", "size_in_gb", "managing_group", "readonly_group")((name: String, location: String, size: Int, managing: LDAPRegistration, readonly: Option[LDAPRegistration]) => HiveDatabase(name, location, size, managing, readonly))

}