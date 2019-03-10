package com.heimdali.generators

import java.time.Clock

import cats.effect.Sync
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models._

class DefaultTopicGenerator[F[_]](appConfig: AppConfig,
                                  ldapGroupGenerator: LDAPGroupGenerator[F])
                                 (implicit clock: Clock, F: Sync[F])
  extends TopicGenerator[F] {

  override def topicFor(name: String, partitions: Int, replicationFactor: Int, workspaceRequest: WorkspaceRequest): F[KafkaTopic] = {
    val workspaceSystemName = WorkspaceGenerator.generateName(workspaceRequest.name)
    val registeredName = s"$workspaceSystemName.$name"
    val snakedName = s"${workspaceSystemName}_$name"

    for {
      manager <- ldapGroupGenerator.generate(snakedName, s"cn=$snakedName,${appConfig.ldap.groupPath}", s"role_$snakedName", workspaceRequest)
      readonly <- ldapGroupGenerator.generate(s"${snakedName}_ro", s"cn=${snakedName}_ro,${appConfig.ldap.groupPath}", s"role_${snakedName}_ro", workspaceRequest)
    } yield KafkaTopic(
      registeredName,
      partitions,
      replicationFactor,
      TopicGrant(registeredName, manager, "read,describe"),
      TopicGrant(registeredName, readonly, "read")
    )
  }
}
