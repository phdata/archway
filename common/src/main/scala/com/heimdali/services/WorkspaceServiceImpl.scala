package com.heimdali.services

import java.util.concurrent.Executors

import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import cats.effect.implicits._
import com.heimdali.clients._
import com.heimdali.models._
import com.heimdali.AppContext
import com.heimdali.provisioning.{ExceptionMessage, Message, SimpleMessage}
import com.heimdali.repositories.{MemberRepository, _}
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

import scala.concurrent.ExecutionContext

class WorkspaceServiceImpl[F[_] : ConcurrentEffect : ContextShift](ldapClient: LDAPClient[F],
                                                                   yarnRepository: YarnRepository,
                                                                   hiveDatabaseRepository: HiveAllocationRepository,
                                                                   ldapRepository: LDAPRepository,
                                                                   workspaceRepository: WorkspaceRequestRepository,
                                                                   complianceRepository: ComplianceRepository,
                                                                   approvalRepository: ApprovalRepository,
                                                                   transactor: Transactor[F],
                                                                   memberRepository: MemberRepository,
                                                                   topicRepository: KafkaTopicRepository,
                                                                   applicationRepository: ApplicationRepository,
                                                                   context: AppContext[F],
                                                                   provisioningService: ProvisioningService[F])
  extends WorkspaceService[F]
    with LazyLogging {

  def fillHive(dbs: List[HiveAllocation]): F[List[HiveAllocation]] =
    dbs.map {
      case hive if hive.directoryCreated.isDefined =>
        context
          .hdfsClient
          .getConsumption(hive.location)
          .map(consumed => hive.copy(consumedInGB = Some(consumed)))
      case hive => Effect[F].pure(hive)
    }.sequence

  override def find(id: Long): OptionT[F, WorkspaceRequest] =
    OptionT(fill(workspaceRepository.find(id)).value.transact(transactor))
      .flatMap(wr => OptionT.liftF(fillHive(wr.data).map(hive => wr.copy(data = hive))))

  private def fill(maybeWorkspace: OptionT[ConnectionIO, WorkspaceRequest]) =
    for {
      workspace <- maybeWorkspace
      datas <- OptionT.liftF(hiveDatabaseRepository.findByWorkspace(workspace.id.get))
      yarns <- OptionT.liftF(yarnRepository.findByWorkspaceId(workspace.id.get))
      appr <- OptionT.liftF(approvalRepository.findByWorkspaceId(workspace.id.get))
      tops <- OptionT.liftF(topicRepository.findByWorkspaceId(workspace.id.get))
      apps <- OptionT.liftF(applicationRepository.findByWorkspaceId(workspace.id.get))
    } yield workspace.copy(data = datas, processing = yarns, approvals = appr, kafkaTopics = tops, applications = apps)

  override def list(distinguishedName: String): F[List[WorkspaceSearchResult]] =
    workspaceRepository.list(distinguishedName).transact(transactor)

  def create(workspace: WorkspaceRequest): F[WorkspaceRequest] =
    (for {
      compliance <- complianceRepository.create(workspace.compliance)
      updatedWorkspace = workspace.copy(compliance = compliance)
      newWorkspaceId <- workspaceRepository.create(updatedWorkspace)

      insertedHive <- workspace.data.traverse[ConnectionIO, HiveAllocation] {
        db =>
          for {
            managerLdap <- ldapRepository.create(db.managingGroup.ldapRegistration)
            managerId <- context.databaseGrantRepository.create(managerLdap.id.get)
            manager = db.managingGroup.copy(id = Some(managerId), ldapRegistration = managerLdap)

            _ <- memberRepository.create(workspace.requestedBy, managerLdap.id.get)

            readwrite <- db.readWriteGroup.map { group =>
              for {
                ldap <- ldapRepository.create(group.ldapRegistration)
                grant <- context.databaseGrantRepository.create(ldap.id.get)
              } yield group.copy(id = Some(grant), ldapRegistration = ldap)
            }.sequence[ConnectionIO, HiveGrant]

            readonly <- db.readonlyGroup.map { group =>
              for {
                ldap <- ldapRepository.create(group.ldapRegistration)
                grant <- context.databaseGrantRepository.create(ldap.id.get)
              } yield group.copy(id = Some(grant), ldapRegistration = ldap)
            }.sequence[ConnectionIO, HiveGrant]

            beforeCreate = db.copy(managingGroup = manager, readWriteGroup = readwrite, readonlyGroup = readonly)
            newHiveId <- hiveDatabaseRepository.create(beforeCreate)
            _ <- workspaceRepository.linkHive(newWorkspaceId, newHiveId)
          } yield beforeCreate.copy(id = Some(newHiveId))
      }

      insertedYarn <- workspace.processing.traverse[ConnectionIO, Yarn] {
        yarn =>
          for {
            newYarnId <- yarnRepository.create(yarn)
            _ <- workspaceRepository.linkPool(newWorkspaceId, newYarnId)
          } yield yarn.copy(id = Some(newYarnId))
      }

      insertedApplications <- workspace.applications.traverse[ConnectionIO, Application] { app =>
        for {
          appLdap <- ldapRepository.create(app.group)
          newApplicationId <- applicationRepository.create(app.copy(group = appLdap))
          _ <- workspaceRepository.linkApplication(newWorkspaceId, newApplicationId)
          _ <- memberRepository.create(workspace.requestedBy, appLdap.id.get)
        } yield app.copy(id = Some(newApplicationId))
      }

      insertedTopics <- workspace.kafkaTopics.traverse[ConnectionIO, KafkaTopic] { topic =>
        for {
          managerLdap <- ldapRepository.create(topic.managingRole.ldapRegistration)
          managerId <- context.topicGrantRepository.create(topic.managingRole.copy(ldapRegistration = managerLdap))
          manager = topic.managingRole.copy(id = Some(managerId), ldapRegistration = managerLdap)

          _ <- memberRepository.create(workspace.requestedBy, managerLdap.id.get)

          readonlyLdap <- ldapRepository.create(topic.readonlyRole.ldapRegistration)
          readonlyId <- context.topicGrantRepository.create(topic.readonlyRole.copy(ldapRegistration = readonlyLdap))
          readonly = topic.readonlyRole.copy(id = Some(readonlyId), ldapRegistration = readonlyLdap)

          beforeCreate = topic.copy(managingRole = manager, readonlyRole = readonly)
          newTopicId <- topicRepository.create(beforeCreate)
          _ <- workspaceRepository.linkTopic(newWorkspaceId, newTopicId)
        } yield beforeCreate.copy(id = Some(newTopicId))
      }
    } yield updatedWorkspace.copy(id = Some(newWorkspaceId), data = insertedHive, processing = insertedYarn, applications = insertedApplications, kafkaTopics = insertedTopics))
      .transact(transactor)

  override def approve(id: Long, approval: Approval): F[Approval] =
    for {
      approval <- approvalRepository.create(id, approval).transact(transactor)
      maybeWorkspace <- find(id).value
      _ <- maybeWorkspace.map(provisioningService.attemptProvision(_, context.appConfig.approvers.required)).getOrElse(().pure[F])
    } yield approval

  override def findByUsername(distinguishedName: String): OptionT[F, WorkspaceRequest] =
    OptionT(fill(workspaceRepository.findByUsername(distinguishedName)).value.transact(transactor))
      .flatMap(wr => OptionT.liftF(fillHive(wr.data).map(hive => wr.copy(data = hive))))

  override def yarnInfo(id: Long): F[List[YarnInfo]] =
    yarnRepository.findByWorkspaceId(id).transact(transactor).flatMap(_.map { yarn =>
      context.yarnClient.applications(yarn.poolName).map(apps => YarnInfo(yarn.poolName, apps))
    }.sequence)

  override def hiveDetails(id: Long): F[List[HiveDatabase]] =
    for {
      datas <- hiveDatabaseRepository.findByWorkspace(id).transact(transactor)
      result <- datas.map {
        case h if h.databaseCreated.isDefined => OptionT.liftF(context.hiveClient.describeDatabase(h.name))
        case _ => OptionT.none[F, HiveDatabase]
      }.traverse(_.value).map(_.flatten)
    } yield result

  override def reviewerList(role: ApproverRole): F[List[WorkspaceSearchResult]] =
    workspaceRepository.pendingQueue(role).transact(transactor)

}