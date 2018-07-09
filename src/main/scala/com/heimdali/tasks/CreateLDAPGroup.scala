package com.heimdali.tasks

import cats.Show
import cats.data.{Kleisli, NonEmptyList}
import cats.effect.IO
import cats.implicits._
import com.heimdali.clients.{GeneralError, GroupAlreadyExists}
import com.heimdali.models.AppConfig

case class CreateLDAPGroup(groupId: Long, commonName: String, distinguishedName: String)

object CreateLDAPGroup {

  implicit val show: Show[CreateLDAPGroup] =
    Show.show(c => s"creating ${c.commonName} group using gid ${c.groupId} at ${c.distinguishedName}}")

  implicit val provisioner: ProvisionTask[CreateLDAPGroup] =
    createLDAPGroup => Kleisli[IO, AppConfig, ProvisionResult] { config =>
      config
        .lDAPClient
        .createGroup(
          createLDAPGroup.groupId,
          createLDAPGroup.commonName,
          createLDAPGroup.distinguishedName)
          .value
        .map {
          case Left(GroupAlreadyExists) => Success[CreateLDAPGroup]("a group already existsed with that name")
          case Left(GeneralError(error)) => Error[CreateLDAPGroup](error)
          case Right(_) => Success[CreateLDAPGroup]
        }
    }
}