package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.data.Kleisli
import cats.effect.{Effect, Timer}
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.models.DatabaseRole
import doobie.implicits._

case class GrantDatabaseAccess(id: Long, roleName: String, databaseName: String, databaseRole: DatabaseRole)

object GrantDatabaseAccess {

  implicit val show: Show[GrantDatabaseAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to ${g.databaseName}")

  implicit def provisioner[F[_] : Effect : Timer]: ProvisionTask[F, GrantDatabaseAccess] =
    ProvisionTask.instance { grant =>
      Kleisli[F, WorkspaceContext[F], ProvisionResult] { case (id, context) =>
        context
          .sentryClient
          .enableAccessToDB(grant.databaseName, grant.roleName, grant.databaseRole)
          .attempt
          .flatMap {
            case Left(exception) => Effect[F].pure(Error(id, grant, exception))
            case Right(_) =>
              for {
                time <- Timer[F].clock.realTime(scala.concurrent.duration.MILLISECONDS)
                _ <- context
                  .databaseGrantRepository
                  .databaseGranted(grant.id, Instant.ofEpochMilli(time))
                  .transact(context.transactor)
              } yield Success(id, grant)
          }
      }
    }

}
