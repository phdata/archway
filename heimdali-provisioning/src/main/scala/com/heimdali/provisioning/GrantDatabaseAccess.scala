package com.heimdali.provisioning

import cats.implicits._
import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import doobie.implicits._
import com.heimdali.AppContext
import com.heimdali.models.DatabaseRole

case class GrantDatabaseAccess(id: Long, roleName: String, databaseName: String, databaseRole: DatabaseRole)

object GrantDatabaseAccess {

  implicit val show: Show[GrantDatabaseAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to ${g.databaseName}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, GrantDatabaseAccess] =
    ProvisionTask.instance { grant =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        F.flatMap(F.attempt(config.sentryClient.enableAccessToDB(grant.databaseName, grant.roleName, grant.databaseRole))) {
          case Left(exception) => F.pure(Error(grant, exception))
          case Right(_) =>
            config
              .databaseGrantRepository
              .databaseGranted(grant.id)
              .transact(config.transactor)
              .map(_ => Success(grant))
        }
      }
    }

}
