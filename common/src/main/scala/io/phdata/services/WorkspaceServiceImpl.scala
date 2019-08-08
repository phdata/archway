package io.phdata.services

import cats.data._
import cats.effect._
import cats.implicits._
import io.phdata.AppContext
import io.phdata.models._
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._

class WorkspaceServiceImpl[F[_]: ConcurrentEffect: ContextShift](
    provisioningService: ProvisioningService[F],
    context: AppContext[F]
) extends WorkspaceService[F] with LazyLogging {

  def fillHive(dbs: List[HiveAllocation]): F[List[HiveAllocation]] =
    dbs.map {
      case hive if hive.directoryCreated.isDefined =>
        context.hdfsClient.getConsumption(hive.location).map(consumed => hive.copy(consumedInGB = Some(consumed)))
      case hive => Effect[F].pure(hive)
    }.sequence

  override def find(id: Long): OptionT[F, WorkspaceRequest] =
    OptionT(
      context.workspaceRequestRepository.find(id).value.transact(context.transactor).flatMap {
        case Some(workspace) =>
          for {
            full <- fill(workspace).transact(context.transactor)
            hive <- fillHive(full.data)
          } yield Some(full.copy(data = hive))
        case None =>
          None.asInstanceOf[Option[WorkspaceRequest]].pure[F]
      }
    )

  private def fill(workspace: WorkspaceRequest): ConnectionIO[WorkspaceRequest] =
    for {
      datas <- context.databaseRepository.findByWorkspace(workspace.id.get)
      yarns <- context.yarnRepository.findByWorkspaceId(workspace.id.get)
      appr <- context.approvalRepository.findByWorkspaceId(workspace.id.get)
      tops <- context.kafkaRepository.findByWorkspaceId(workspace.id.get)
      apps <- context.applicationRepository.findByWorkspaceId(workspace.id.get)
    } yield workspace.copy(data = datas, processing = yarns, approvals = appr, kafkaTopics = tops, applications = apps)

  override def list(distinguishedName: String): F[List[WorkspaceSearchResult]] =
    context.workspaceRequestRepository.list(distinguishedName).transact(context.transactor)

  override def userAccessible(distinguishedName: DistinguishedName, id: Long): F[Boolean] =
    for {
      accessible <- context.workspaceRequestRepository.userAccessible(distinguishedName).transact(context.transactor)
    } yield accessible.contains(id)

  def create(workspace: WorkspaceRequest): F[WorkspaceRequest] = {
    val createResult = for {
      compliance <- context.complianceRepository.create(workspace.compliance)
      updatedWorkspace = workspace.copy(compliance = compliance)
      newWorkspaceId <- context.workspaceRequestRepository.create(updatedWorkspace)

      _ <- workspace.data.traverse[ConnectionIO, Unit] { db =>
        for {
          managerLdap <- context.ldapRepository.create(db.managingGroup.ldapRegistration)
          managerId <- context.databaseGrantRepository.create(managerLdap.id.get)
          manager = db.managingGroup.copy(id = Some(managerId), ldapRegistration = managerLdap)

          _ <- context.memberRepository.create(workspace.requestedBy.value, managerLdap.id.get)

          readwrite <- db.readWriteGroup
            .map { group =>
              for {
                ldap <- context.ldapRepository.create(group.ldapRegistration)
                grant <- context.databaseGrantRepository.create(ldap.id.get)
              } yield group.copy(id = Some(grant), ldapRegistration = ldap)
            }
            .sequence[ConnectionIO, HiveGrant]

          readonly <- db.readonlyGroup
            .map { group =>
              for {
                ldap <- context.ldapRepository.create(group.ldapRegistration)
                grant <- context.databaseGrantRepository.create(ldap.id.get)
              } yield group.copy(id = Some(grant), ldapRegistration = ldap)
            }
            .sequence[ConnectionIO, HiveGrant]

          beforeCreate = db.copy(managingGroup = manager, readWriteGroup = readwrite, readonlyGroup = readonly)
          newHiveId <- context.databaseRepository.create(beforeCreate)
          _ <- context.workspaceRequestRepository.linkHive(newWorkspaceId, newHiveId)
        } yield beforeCreate.copy(id = Some(newHiveId))
      }

      _ <- workspace.processing.traverse[ConnectionIO, Unit] { yarn =>
        for {
          newYarnId <- context.yarnRepository.create(yarn)
          _ <- context.workspaceRequestRepository.linkPool(newWorkspaceId, newYarnId)
        } yield ()
      }

      _ <- workspace.applications.traverse[ConnectionIO, Unit] { app =>
        for {
          appLdap <- context.ldapRepository.create(app.group)
          newApplicationId <- context.applicationRepository.create(app.copy(group = appLdap))
          _ <- context.workspaceRequestRepository.linkApplication(newWorkspaceId, newApplicationId)
          _ <- context.memberRepository.create(workspace.requestedBy.value, appLdap.id.get)
        } yield ()
      }

      _ <- workspace.kafkaTopics.traverse[ConnectionIO, KafkaTopic] { topic =>
        for {
          managerLdap <- context.ldapRepository.create(topic.managingRole.ldapRegistration)
          managerId <- context.topicGrantRepository.create(topic.managingRole.copy(ldapRegistration = managerLdap))
          manager = topic.managingRole.copy(id = Some(managerId), ldapRegistration = managerLdap)

          _ <- context.memberRepository.create(workspace.requestedBy.value, managerLdap.id.get)

          readonlyLdap <- context.ldapRepository.create(topic.readonlyRole.ldapRegistration)
          readonlyId <- context.topicGrantRepository.create(topic.readonlyRole.copy(ldapRegistration = readonlyLdap))
          readonly = topic.readonlyRole.copy(id = Some(readonlyId), ldapRegistration = readonlyLdap)

          beforeCreate = topic.copy(managingRole = manager, readonlyRole = readonly)
          newTopicId <- context.kafkaRepository.create(beforeCreate)
          _ <- context.workspaceRequestRepository.linkTopic(newWorkspaceId, newTopicId)
        } yield beforeCreate.copy(id = Some(newTopicId))
      }
    } yield newWorkspaceId

    createResult.transact(context.transactor).flatMap(workspaceId => find(workspaceId).value.map(_.get))
  }

  override def approve(id: Long, approval: Approval): F[Approval] =
    for {
      approval <- context.approvalRepository.create(id, approval).transact(context.transactor)
      maybeWorkspace <- find(id).value
      _ <- maybeWorkspace
        .map(provisioningService.attemptProvision(_, context.appConfig.approvers.required))
        .getOrElse(().pure[F])
    } yield approval

  override def status(id: Long): F[WorkspaceStatus] = {
    for {
      unProvisionedWorkspaces <- provisioningService.findUnprovisioned()
      unProvisionedIds <- unProvisionedWorkspaces.map(_.id.get).pure[F]
    } yield {
      if (!unProvisionedIds.contains(id)) {
        WorkspaceStatus(WorkspaceProvisioningStatus.COMPLETED)
      } else {
        WorkspaceStatus(WorkspaceProvisioningStatus.PENDING)
      }
    }
  }

  override def findByUsername(distinguishedName: String): OptionT[F, WorkspaceRequest] =
    OptionT(
      context.workspaceRequestRepository.findByUsername(distinguishedName).value.transact(context.transactor).flatMap {
        case Some(workspace) =>
          for {
            full <- fill(workspace).transact(context.transactor)
            hive <- fillHive(full.data)
          } yield Some(full.copy(data = hive))
        case None =>
          None.asInstanceOf[Option[WorkspaceRequest]].pure[F]
      }
    )

  override def yarnInfo(id: Long): F[List[YarnInfo]] =
    context.yarnRepository.findByWorkspaceId(id).transact(context.transactor).flatMap { workspace =>
      workspace.traverse(yarn =>
        context.yarnClient.applications(yarn.poolName).map(apps => YarnInfo(yarn.poolName, apps)))
    }

  override def hiveDetails(id: Long): F[List[HiveDatabase]] =
    for {
      datas <- context.databaseRepository.findByWorkspace(id).transact(context.transactor)
      result <- datas
        .map {
          case h if h.databaseCreated.isDefined => OptionT.liftF(context.hiveClient.describeDatabase(h.name))
          case _                                => OptionT.none[F, HiveDatabase]
        }
        .traverse(_.value)
        .map(_.flatten)
    } yield result

  override def reviewerList(role: ApproverRole): F[List[WorkspaceSearchResult]] =
    context.workspaceRequestRepository.pendingQueue(role).transact(context.transactor)

  override def deleteWorkspace(workspaceId: Long): F[Unit] = {
    context.workspaceRequestRepository.deleteWorkspace(workspaceId).transact(context.transactor).void
  }

}
