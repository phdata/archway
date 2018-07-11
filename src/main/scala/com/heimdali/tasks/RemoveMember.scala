package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class RemoveMember(groupDN: String, userDN: String)

object RemoveMember {

  implicit val show: Show[RemoveMember] =
    Show.show(r => s"""removing "${r.userDN}" from "${r.groupDN}""")

  implicit val provisioner: ProvisionTask[RemoveMember] =
    remove => Kleisli[IO, AppConfig, ProvisionResult] { config =>
      config.ldapClient.removeUser(remove.groupDN, remove.userDN).value.attempt.map {
        case Left(exception) => Error(exception)
        case Right(Some(_)) => Success[RemoveMember]
        case Right(None) => Success(s"${remove.userDN} wasn't a member of ${remove.groupDN}")
      }
    }

}
