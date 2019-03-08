package com.heimdali.generators

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import com.heimdali.config.{AppConfig, LDAPConfig}
import com.heimdali.models._

class DefaultTopicGenerator[F[_]](appConfig: AppConfig)
                                 (implicit F: Sync[F])
  extends TopicGenerator[F]
    with AttributeGenerator[F] {

  val ldapConfig: LDAPConfig = appConfig.ldap

  override def topicFor(name: String, partitions: Int, replicationFactor: Int, workspaceSystemName: String): F[KafkaTopic] = {
    val registeredName = s"$workspaceSystemName.$name"
    val snakedName = s"${workspaceSystemName}_$name"

    for {
      manager <- generate(snakedName, s"cn=$snakedName,${appConfig.ldap.groupPath}", s"role_$snakedName")
      readonly <- generate(s"${snakedName}_ro", s"cn=${snakedName}_ro,${appConfig.ldap.groupPath}", s"role_${snakedName}_ro")
    } yield KafkaTopic(
      registeredName,
      partitions,
      replicationFactor,
      TopicGrant(registeredName, manager, "read,describe"),
      TopicGrant(registeredName, readonly, "read")
    )
  }
}
