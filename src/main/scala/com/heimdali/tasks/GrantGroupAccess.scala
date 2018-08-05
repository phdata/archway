package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.models.AppContext
import doobie.implicits._

case class GrantGroupAccess(ldapId: Long, roleName: String, groupName: String)

object GrantGroupAccess {

  implicit val show: Show[GrantGroupAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to group ${g.groupName}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, GrantGroupAccess] =
    ProvisionTask.instance { grant =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        F.flatMap(F.attempt(config.hiveClient.grantGroup(grant.groupName, grant.roleName))) {
          case Left(exception) => F.pure(Error(grant, exception))
          case Right(_) =>
            F.map(config
              .ldapRepository
              .groupAssociated(grant.ldapId)
              .transact(config.transactor)) { _ => Success(grant) }
        }
      }
    }

}
