package com.heimdali.models

import java.time.Instant

import cats.{FlatMap, Show}
import cats.data.{EitherT, Kleisli, OptionT}
import cats.effect.IO
import org.apache.hadoop.fs.Path
import scalaz.NonEmptyList

case class KafkaTopic(name: String, partitions: Int, replicationFactor: Int)

case class AppConfig()

sealed trait Provision[A] {
  def provision(provisionable: A): Kleisli[OptionT[IO, ?], AppConfig, String]
}

case class CreateHiveDatabase(name: String, location: Path)

object Provision {

  def apply[A](implicit provision: Provision[A]): Provision[A] = provision

  def show[A: Provision](a: A) = Provision[A].provision(a)

  implicit class ProvisionOps[A: Provision](a: A) {
    def provision = Provision[A].provision(a)
  }

  implicit val createHiveDatabaseProvisioner: Provision[CreateHiveDatabase] = new Provision[CreateHiveDatabase] {
    override def provision(provisionable: CreateHiveDatabase): Kleisli[OptionT[IO, ?], AppConfig, String] =
      Kleisli[OptionT[IO, ?], AppConfig, String](_ => OptionT.some[IO](""))
  }

}

object Something {
  import Provision._
  val db = CreateHiveDatabase("", new Path(""))
  implicitly[Provision[CreateHiveDatabase]]
  db.provision(AppConfig())
}

case class CreateRole(name: String)
case class GrantGroupAccess(role: String, groupName: String)
case class GrantDatabaseAccess(role: String, databaseName: String)
case class GrantLocationAccess(role: String, location: Path)
case class AllowDatabaseAccess(databaseName: String, roleName: String)
case class SetDiskQuota(location: String, sizeInGB: Int)
case class CreateLDAPGroup(distinguishedName: String)
case class AddMember(groupDN: String, userDN: String)
case class RemoveMember(groupDN: String, userDN: String)
case class CreateResourcePool(name: String, cores: Int, memory: Int)
