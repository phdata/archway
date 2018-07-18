package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import doobie.implicits._
import com.heimdali.models.AppContext
import com.heimdali.repositories.DatabaseRole

case class GrantDatabaseAccess(id: Long, roleName: String, databaseName: String)

object GrantDatabaseAccess {

  implicit val show: Show[GrantDatabaseAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to ${g.databaseName}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, GrantDatabaseAccess] =
    ProvisionTask.instance { grant =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        F.flatMap(F.attempt(config.hiveClient.enableAccessToDB(grant.databaseName, grant.roleName))) {
          case Left(exception) => F.pure(Error(exception))
          case Right(_) =>
            F.map(config
              .databaseGrantRepository
              .databaseGranted(grant.id)
              .transact(config.transactor)) {_ => Success[GrantDatabaseAccess] }
        }
      }
    }

}
