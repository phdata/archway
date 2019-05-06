package com.heimdali.generators

import cats.effect.{Clock, Sync}
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models._
import com.heimdali.services.TemplateService

class DefaultTopicGenerator[F[_]](appConfig: AppConfig,
                                  ldapGroupGenerator: LDAPGroupGenerator[F])
                                 (implicit clock: Clock[F], F: Sync[F])
  extends TopicGenerator[F] {

  override def topicFor(name: String, partitions: Int, replicationFactor: Int, workspaceRequest: WorkspaceRequest): F[KafkaTopic] = {
    val workspaceSystemName = TemplateRequest.generateName(workspaceRequest.name)
    val topicSystemName = TemplateRequest.generateName(name)
    val registeredName = s"$workspaceSystemName.$topicSystemName"
    val managerName = s"${workspaceSystemName}_$topicSystemName"
    val readonlyName = s"${workspaceSystemName}_${topicSystemName}_ro"

    for {
      manager <- ldapGroupGenerator.generate(managerName, s"cn=$managerName,${appConfig.ldap.groupPath}", s"role_$managerName", workspaceRequest)
      readonly <- ldapGroupGenerator.generate(readonlyName, s"cn=$readonlyName,${appConfig.ldap.groupPath}", s"role_$readonlyName", workspaceRequest)
    } yield KafkaTopic(
      registeredName,
      partitions,
      replicationFactor,
      TopicGrant(registeredName, manager, "read,describe"),
      TopicGrant(registeredName, readonly, "read")
    )
  }
}
