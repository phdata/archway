package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.data._
import cats.effect.{Effect, Timer}
import cats.implicits._
import com.heimdali.AppContext
import doobie.implicits._

case class CreateLDAPGroup(groupId: Long, commonName: String, distinguishedName: String, attributes: List[(String, String)])

object CreateLDAPGroup {

  implicit val show: Show[CreateLDAPGroup] =
    Show.show(c => s"creating ${c.commonName} group using gid ${c.groupId} at ${c.distinguishedName}}")

  implicit def provisioner[F[_] : Effect : Timer]: ProvisionTask[F, CreateLDAPGroup] =
    ProvisionTask.instance[F, CreateLDAPGroup] { create =>
      Kleisli[F, WorkspaceContext[F], ProvisionResult] { case (id, context) =>
        context
          .provisioningLDAPClient
          .createGroup(create.commonName, create.attributes)
          .attempt
          .flatMap {
            case Left(exception) => Effect[F].pure(Error(id, create, exception))
            case Right(_) =>
              for {
                time <- Timer[F].clock.realTime(scala.concurrent.duration.MILLISECONDS)
                _ <- context
                  .ldapRepository
                  .groupCreated(create.groupId, Instant.ofEpochMilli(time))
                  .transact(context.transactor)
              } yield Success(id, create)
          }
      }
    }
}
