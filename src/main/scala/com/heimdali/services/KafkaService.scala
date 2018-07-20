package com.heimdali.services

import cats._
import cats.data._
import cats.implicits._
import cats.effect.Effect
import com.heimdali.clients.KafkaClient
import com.heimdali.models._
import com.heimdali.tasks.Success
import com.heimdali.tasks.ProvisionResult
import com.heimdali.tasks.ProvisionTask._
import com.heimdali.repositories.KafkaRepository
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._

trait KafkaService[F[_]] {

  def create(workspaceId: Long, databaseId: Long, kafkaTopic: TopicRequest): F[KafkaTopic]

}

class KafkaServiceImpl[F[_] : Effect](appContext: AppContext[F])
    extends KafkaService[F]
    with LazyLogging {
  override def create(workspaceId: Long, databaseId: Long, topicRequest: TopicRequest): F[KafkaTopic] =
    (for {
       workspace <- OptionT(
         appContext
           .workspaceRequestRepository
           .find(workspaceId)
           .value
           .transact(appContext.transactor)
       )

       _ <- OptionT.pure[F](logger.info("found {}", workspace))

       database <- OptionT(
         appContext
           .databaseRepository
           .find(databaseId)
           .value
           .transact(appContext.transactor)
       )

       _ <- OptionT.pure[F](logger.info("found {}", database))

       topicName = s"${database.name}_${Generator.generateName(topicRequest.name)}"
       kafkaTopic = KafkaTopic(
         topicName,
         topicRequest.partitions,
         topicRequest.replicationFactor,
         TopicGrant(topicName, LDAPRegistration(s"${topicName},${appContext.appConfig.ldap.groupPath}", topicName, s"role_${topicName}"), "read,describe"),
         TopicGrant(topicName, LDAPRegistration(s"${topicName}_ro,${appContext.appConfig.ldap.groupPath}", s"${topicName}_ro", s"role_${topicName}_ro"), "read")
       )

       result <- OptionT.liftF {
         (for {
           managerLDAP <- appContext.ldapRepository.create(kafkaTopic.managingRole.ldapRegistration)
           readonlyLDAP <- appContext.ldapRepository.create(kafkaTopic.readonlyRole.ldapRegistration)

           beforeRoles = kafkaTopic.copy(managingRole = kafkaTopic.managingRole.copy(ldapRegistration = managerLDAP), readonlyRole = kafkaTopic.readonlyRole.copy(ldapRegistration = readonlyLDAP))
           managerRoleId <- appContext.topicGrantRepository.create(beforeRoles.managingRole)
           readonlyRoleId <- appContext.topicGrantRepository.create(beforeRoles.readonlyRole)

           afterRoles = beforeRoles.copy(managingRole = beforeRoles.managingRole.copy(id = Some(managerRoleId)), readonlyRole = beforeRoles.readonlyRole.copy(id = Some(readonlyRoleId)))

           id <- appContext.kafkaRepository.create(afterRoles)

           _ <- appContext.workspaceRequestRepository.linkTopic(workspaceId, id)
          } yield afterRoles.copy(id = Some(id))).transact(appContext.transactor)
       }

       _ <- if(workspace.approved)
       OptionT.liftF(
         kafkaTopic
           .provision
           .run(appContext)
       ) else OptionT.some[F](Success(NonEmptyList.one("")))

    } yield result).value.map(_.get)
}
