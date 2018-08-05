package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import doobie.implicits._
import com.heimdali.clients.{GeneralError, GroupAlreadyExists}
import com.heimdali.models.AppContext

case class CreateLDAPGroup(groupId: Long, commonName: String, distinguishedName: String)

object CreateLDAPGroup {

  implicit val show: Show[CreateLDAPGroup] =
    Show.show(c => s"creating ${c.commonName} group using gid ${c.groupId} at ${c.distinguishedName}}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, CreateLDAPGroup] =
    ProvisionTask.instance[F, CreateLDAPGroup] { create =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        F.flatMap(F.map(config.ldapClient.createGroup(create.groupId, create.commonName, create.distinguishedName).value) {
          case Right(_) => Success(create)
          case Left(GroupAlreadyExists) => Success(create, "a group already existsed with that name")
          case Left(GeneralError(error)) => Error(create, error)
        }) {
          case out: Success =>
            F.map(config
              .ldapRepository
              .groupCreated(create.groupId)
              .transact(config.transactor)) { _ => out }
          case out => F.pure(out)
        }
      }
    }
}
