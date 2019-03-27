package com.heimdali.provisioning

import cats._
import cats.implicits._
import cats.effect._
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.AppContext
import doobie.implicits._

case class AddMember(ldapRegistrationId: Long, groupDN: String, distinguishedName: String)

object AddMember {

  implicit val show: Show[AddMember] =
    Show.show(am => s"""adding "${am.distinguishedName}" to "${am.groupDN}""")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, AddMember] =
    ProvisionTask.instance { add =>
      Kleisli[F, WorkspaceContext[F], ProvisionResult] { case (id, context) =>
        context
          .ldapClient
          .addUser(add.groupDN, add.distinguishedName)
          .value
          .attempt
          .map {
            case Left(exception) => Error(id, add, exception)
            case Right(Some(_)) => Success(id, add)
            case Right(None) => Success(id, add, s"${add.distinguishedName} already belongs to ${add.groupDN}")
          }
          .flatMap {
            case out: Success =>
              F.map(context
                .memberRepository
                .complete(add.ldapRegistrationId, add.distinguishedName)
                .transact(context.transactor)) { _ => out }
          }
      }
    }
}
