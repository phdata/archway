package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.models.AppConfig

case class GrantLocationAccess(roleName: String, location: String)

object GrantLocationAccess {

  implicit val show: Show[GrantLocationAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to location ${g.location}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, GrantLocationAccess] =
    ProvisionTask.instance { grant =>
      Kleisli[F, AppConfig[F], ProvisionResult] { config =>
        F.map(F.attempt(config.hiveClient.enableAccessToLocation(grant.location, grant.roleName))) {
          case Left(exception) => Error(exception)
          case Right(_) => Success[GrantLocationAccess]
        }
      }
    }

}
