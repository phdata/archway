package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.clients.{GeneralError, GroupAlreadyExists}
import com.heimdali.models.AppConfig

case class CreateLDAPGroup(groupId: Long, commonName: String, distinguishedName: String)

object CreateLDAPGroup {

  implicit val show: Show[CreateLDAPGroup] =
    Show.show(c => s"creating ${c.commonName} group using gid ${c.groupId} at ${c.distinguishedName}}")

  implicit val provisioner: ProvisionTask[CreateLDAPGroup] =
    create => Kleisli[IO, AppConfig, ProvisionResult] { config =>
      config
        .ldapClient
        .createGroup(
          create.groupId,
          create.commonName,
          create.distinguishedName)
          .value
        .map {
          case Right(_) => Success[CreateLDAPGroup]
          case Left(GroupAlreadyExists) => Success("a group already existsed with that name")
          case Left(GeneralError(error)) => Error(error)
        }
    }
}
