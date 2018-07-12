package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.models.AppConfig

case class GrantDatabaseAccess(roleName: String, databaseName: String)

object GrantDatabaseAccess {

  implicit val show: Show[GrantDatabaseAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to ${g.databaseName}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, GrantDatabaseAccess] =
    ProvisionTask.instance { grant =>
      Kleisli[F, AppConfig[F], ProvisionResult] { config =>
        F.map(F.attempt(config.hiveClient.enableAccessToDB(grant.databaseName, grant.roleName))) {
          case Left(exception) => Error(exception)
          case Right(_) => Success[GrantDatabaseAccess]
        }
      }
    }

}
