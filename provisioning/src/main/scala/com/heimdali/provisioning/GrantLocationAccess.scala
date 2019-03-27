package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.data.Kleisli
import cats.effect.{Effect, Timer}
import cats.implicits._
import com.heimdali.AppContext
import doobie.implicits._

case class GrantLocationAccess(id: Long, roleName: String, location: String)

object GrantLocationAccess {

  implicit val show: Show[GrantLocationAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to location ${g.location}")

  implicit def provisioner[F[_] : Effect : Timer]: ProvisionTask[F, GrantLocationAccess] =
    ProvisionTask.instance { grant =>
      Kleisli[F, WorkspaceContext[F], ProvisionResult] { case (id, context) =>
        context
          .sentryClient
          .enableAccessToLocation(grant.location, grant.roleName)
          .attempt
          .flatMap {
            case Left(exception) => Effect[F].pure(Error(id, grant, exception))
            case Right(_) =>
              for {
                time <- Timer[F].clock.realTime(scala.concurrent.duration.MILLISECONDS)
                _ <- context
                  .databaseGrantRepository
                  .locationGranted(grant.id, Instant.ofEpochMilli(time))
                  .transact(context.transactor)
              } yield Success(id, grant)
          }
      }
    }

}
