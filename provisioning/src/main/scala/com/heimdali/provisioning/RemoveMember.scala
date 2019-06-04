package com.heimdali.provisioning

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.AppContext

case class RemoveMember(groupDN: String, userDN: String)

object RemoveMember {

  implicit val show: Show[RemoveMember] =
    Show.show(r => s"""removing "${r.userDN}" from "${r.groupDN}""")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, RemoveMember] =
    ProvisionTask.instance { remove =>
      Kleisli[F, WorkspaceContext[F], ProvisionResult] { case (id, context) =>
        F.map(F.attempt(context.provisioningLDAPClient.removeUser(remove.groupDN, remove.userDN).value)) {
          case Left(exception) => Error(id, remove, exception)
          case Right(Some(_)) => Success(id, remove)
          case Right(None) => Success(id, remove, s"${remove.userDN} wasn't a member of ${remove.groupDN}")
        }
      }
    }

}
