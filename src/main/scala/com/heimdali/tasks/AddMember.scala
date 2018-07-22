package com.heimdali.tasks

import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.models.AppContext
import doobie.implicits._

case class AddMember(id: Long, groupDN: String, username: String)

object AddMember {

  implicit val show: Show[AddMember] =
    Show.show(am => s"""adding "${am.username}" to "${am.groupDN}""")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, AddMember] =
    ProvisionTask.instance { add =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        config
          .ldapClient
          .addUser(add.groupDN, add.username)
          .value
          .attempt
          .map {
            case Left(exception) => Error(add, exception)
            case Right(Some(_)) => Success(add)
            case Right(None) => Success(add, s"${add.username} already belongs to ${add.groupDN}")
          }
          .flatMap {
            case out: Success =>
              F.map(config
                .memberRepository
                .complete(add.id)
                .transact(config.transactor)) { _ => out }
          }
      }
    }
}
