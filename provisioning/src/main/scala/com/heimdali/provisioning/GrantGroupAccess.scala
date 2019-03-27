package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.data.Kleisli
import cats.implicits._
import cats.effect.{Effect, Timer}
import com.heimdali.AppContext
import doobie.implicits._

case class GrantGroupAccess(ldapId: Long, roleName: String, groupName: String)

object GrantGroupAccess {

  implicit val show: Show[GrantGroupAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to group ${g.groupName}")

  implicit def provisioner[F[_] : Effect : Timer]: ProvisionTask[F, GrantGroupAccess] =
    ProvisionTask.instance { grant =>
      Kleisli[F, WorkspaceContext[F], ProvisionResult] { case (id, context) =>
        context
          .sentryClient
          .grantGroup(grant.groupName, grant.roleName)
          .attempt
          .flatMap {
            case Left(exception) => Effect[F].pure(Error(id, grant, exception))
            case Right(_) =>
              for {
                time <- Timer[F].clock.realTime(scala.concurrent.duration.MILLISECONDS)
                _ <- context
                  .ldapRepository
                  .groupAssociated(grant.ldapId, Instant.ofEpochMilli(time))
                  .transact(context.transactor)
              } yield Success(id, grant)
          }
      }
    }

}
