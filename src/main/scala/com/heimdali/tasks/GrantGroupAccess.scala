package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class GrantGroupAccess(roleName: String, groupName: String)

object GrantGroupAccess {

  implicit val show: Show[GrantGroupAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to group ${g.groupName}")

  implicit val provisioner: ProvisionTask[GrantGroupAccess] =
    grant => Kleisli[IO, AppConfig, ProvisionResult[GrantGroupAccess]] { config =>
      config.hiveClient.grantGroup(grant.groupName, grant.roleName).attempt.map {
        case Left(exception) => Error(exception)
        case Right(_) => Success[GrantGroupAccess]
      }
    }

}