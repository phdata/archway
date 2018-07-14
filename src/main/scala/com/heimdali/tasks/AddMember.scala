package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.{Effect, IO}
import cats.syntax.show
import com.heimdali.models.AppContext

case class AddMember(groupDN: String, username: String)

object AddMember {

  implicit val show: Show[AddMember] =
    Show.show(am => s"""adding "${am.username}" to "${am.groupDN}""")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, AddMember] =
    ProvisionTask.instance { add =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        F.map(F.attempt(config.ldapClient.addUser(add.groupDN, add.username).value)) {
          case Left(exception) => Error(exception)
          case Right(Some(_)) => Success[AddMember]
          case Right(None) => Success(s"${add.username} already belongs to ${add.groupDN}")
        }
      }
    }
}
