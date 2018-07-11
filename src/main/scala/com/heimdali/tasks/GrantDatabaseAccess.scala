package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class GrantDatabaseAccess(roleName: String, databaseName: String)

object GrantDatabaseAccess {

  implicit val show: Show[GrantDatabaseAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to ${g.databaseName}")

  implicit val provisioner: ProvisionTask[GrantDatabaseAccess] =
    grant => Kleisli[IO, AppConfig, ProvisionResult] { config =>
      config.hiveClient.enableAccessToDB(grant.databaseName, grant.roleName).attempt.map {
        case Left(exception) => Error(exception)
        case Right(_) => Success[GrantDatabaseAccess]
      }
    }

}
