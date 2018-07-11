package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig
import org.apache.hadoop.fs.Path

case class GrantLocationAccess(roleName: String, location: String)

object GrantLocationAccess {

  implicit val show: Show[GrantLocationAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to location ${g.location}")

  implicit val provisioner: ProvisionTask[GrantLocationAccess] =
    grant => Kleisli[IO, AppConfig, ProvisionResult] { config =>
      config.hiveClient.enableAccessToLocation(grant.location, grant.roleName).attempt.map {
        case Left(exception) => Error(exception)
        case Right(_) => Success[GrantLocationAccess]
      }
    }

}
