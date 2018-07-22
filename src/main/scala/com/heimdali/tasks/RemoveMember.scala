package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.models.AppContext

case class RemoveMember(groupDN: String, userDN: String)

object RemoveMember {

  implicit val show: Show[RemoveMember] =
    Show.show(r => s"""removing "${r.userDN}" from "${r.groupDN}""")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, RemoveMember] =
    ProvisionTask.instance { remove =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        F.map(F.attempt(config.ldapClient.removeUser(remove.groupDN, remove.userDN).value)) {
          case Left(exception) => Error(remove, exception)
          case Right(Some(_)) => Success(remove)
          case Right(None) => Success(remove, s"${remove.userDN} wasn't a member of ${remove.groupDN}")
        }
      }
    }

}
