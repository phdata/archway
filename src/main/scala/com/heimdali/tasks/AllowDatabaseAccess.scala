package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class AllowDatabaseAccess(databaseName: String, roleName: String)

object AllowDatabaseAccess {

  implicit val show: Show[AllowDatabaseAccess] = ???

  implicit val provisioner: ProvisionTask[AllowDatabaseAccess] = ???

}
