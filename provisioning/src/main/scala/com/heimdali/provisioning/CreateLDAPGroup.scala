package com.heimdali.provisioning

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import doobie.implicits._
import com.heimdali.clients.{GeneralError, GroupAlreadyExists}
import com.heimdali.AppContext

case class CreateLDAPGroup(groupId: Long, commonName: String, distinguishedName: String, attributes: List[(String, String)])

object CreateLDAPGroup {

  implicit val show: Show[CreateLDAPGroup] =
    Show.show(c => s"creating ${c.commonName} group using gid ${c.groupId} at ${c.distinguishedName}}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, CreateLDAPGroup] =
    ProvisionTask.instance[F, CreateLDAPGroup] { create =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        F.flatMap(F.attempt(config.ldapClient.createGroup(create.commonName, create.attributes))) {
          case Left(exception) => F.pure(Error(create, exception))
          case Right(_) =>
            F.map(config
              .ldapRepository
              .groupCreated(create.groupId)
              .transact(config.transactor)) { _ => Success(create) }
        }
      }
    }
}
