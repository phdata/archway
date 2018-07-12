package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.models.AppConfig

case class RemoveMember(groupDN: String, userDN: String)

object RemoveMember {

  implicit val show: Show[RemoveMember] =
    Show.show(r => s"""removing "${r.userDN}" from "${r.groupDN}""")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, RemoveMember] =
    ProvisionTask.instance { remove =>
      Kleisli[F, AppConfig[F], ProvisionResult] { config =>
        F.map(F.attempt(config.ldapClient.removeUser(remove.groupDN, remove.userDN).value)) {
          case Left(exception) => Error(exception)
          case Right(Some(_)) => Success[RemoveMember]
          case Right(None) => Success(s"${remove.userDN} wasn't a member of ${remove.groupDN}")
        }
      }
    }

}
