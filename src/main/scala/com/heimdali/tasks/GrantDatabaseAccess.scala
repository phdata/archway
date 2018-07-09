package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class GrantDatabaseAccess(role: String, databaseName: String)

object GrantDatabaseAccess {

  implicit val show: Show[GrantDatabaseAccess] = ???

  implicit val provisioner: ProvisionTask[GrantDatabaseAccess] = ???

}