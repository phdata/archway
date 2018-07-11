package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import cats.syntax.show
import com.heimdali.models.AppConfig

case class AddMember(groupDN: String, username: String)

object AddMember {

  implicit val show: Show[AddMember] =
    Show.show(am => s"""adding "${am.username}" to "${am.groupDN}""")

  implicit val provisioner: ProvisionTask[AddMember] =
    add => Kleisli[IO, AppConfig, ProvisionResult] { config =>
      config.ldapClient.addUser(add.groupDN, add.username).value.attempt.map {
        case Left(exception) => Error(exception)
        case Right(Some(_)) => Success[AddMember]
        case Right(None) => Success(s"${add.username} already belongs to ${add.groupDN}")
      }
    }
}
