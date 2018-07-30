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
import com.heimdali.repositories.KafkaTopicRepository
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._

trait KafkaService[F[_]] {

  def create(username: String, workspaceId: Long, databaseId: Long, kafkaTopic: TopicRequest): F[NonEmptyList[String]]

}

class KafkaServiceImpl[F[_] : Effect](appContext: AppContext[F])
    extends KafkaService[F]
    with LazyLogging {
  override def create(username: String, workspaceId: Long, databaseId: Long, topicRequest: TopicRequest): F[NonEmptyList[String]] =
    (for {
       workspace <- OptionT(
         appContext
           .workspaceRequestRepository
           .find(workspaceId)
           .value
           .transact(appContext.transactor)
       )

       database <- OptionT(
         appContext
           .databaseRepository
           .find(databaseId)
           .value
           .transact(appContext.transactor)
       )

       topicName = s"${database.name}_${Generator.generateName(topicRequest.name)}"
       kafkaTopic = KafkaTopic(
         topicName,
         topicRequest.partitions,
         topicRequest.replicationFactor,
         TopicGrant(topicName, LDAPRegistration(s"cn=${topicName},${appContext.appConfig.ldap.groupPath}", topicName, s"role_${topicName}"), "read,describe"),
         TopicGrant(topicName, LDAPRegistration(s"cn=${topicName}_ro,${appContext.appConfig.ldap.groupPath}", s"${topicName}_ro", s"role_${topicName}_ro"), "read"),
         requestor = Some(username)
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

           _ <- appContext.memberRepository.create(username, managerLDAP.id.get)

           _ <- appContext.workspaceRequestRepository.linkTopic(workspaceId, id)
          } yield afterRoles.copy(id = Some(id))).transact(appContext.transactor)
       }

       provisionResult <- OptionT.liftF(
         result
           .provision
           .run(appContext)
       )

    } yield provisionResult.messages).value.map(_.get)
}
