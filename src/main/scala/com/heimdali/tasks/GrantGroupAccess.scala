package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.models.AppConfig

case class GrantGroupAccess(roleName: String, groupName: String)

object GrantGroupAccess {

  implicit val show: Show[GrantGroupAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to group ${g.groupName}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, GrantGroupAccess] =
    ProvisionTask.instance { grant =>
      Kleisli[F, AppConfig[F], ProvisionResult] { config =>
        F.map(F.attempt(config.hiveClient.grantGroup(grant.groupName, grant.roleName))) {
          case Left(exception) => Error(exception)
          case Right(_) => Success[GrantGroupAccess]
        }
      }
    }

}
