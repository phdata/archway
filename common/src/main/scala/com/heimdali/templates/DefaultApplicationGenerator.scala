package com.heimdali.templates

import cats.Monad
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models.Application

class DefaultApplicationGenerator[F[_] : Monad](appConfig: AppConfig)
  extends ApplicationGenerator[F]
    with AttributeGenerator[F] {

  val ldapConfig = appConfig.ldap

  override def applicationFor(name: String, workspaceSystemName: String): F[Application] = {
    val consumerGroup = s"${workspaceSystemName}_${name}_cg"
    generate(consumerGroup, s"cn=$consumerGroup,${appConfig.ldap.groupPath}", s"role_$consumerGroup").map { ldap =>
      Application(
        name,
        consumerGroup,
        ldap
      )
    }
  }

}
