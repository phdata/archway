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
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        config
          .sentryClient
          .grantGroup(grant.groupName, grant.roleName)
          .attempt
          .flatMap {
            case Left(exception) => Effect[F].pure(Error(grant, exception))
            case Right(_) =>
              for {
                time <- Timer[F].clock.realTime(scala.concurrent.duration.MILLISECONDS)
                _ <- config
                  .ldapRepository
                  .groupAssociated(grant.ldapId, Instant.ofEpochMilli(time))
                  .transact(config.transactor)
              } yield Success(grant)
          }
      }
    }

}
