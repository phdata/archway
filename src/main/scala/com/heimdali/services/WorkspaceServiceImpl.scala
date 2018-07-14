package com.heimdali.services

import java.util.concurrent.Executors

import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.clients._
import com.heimdali.models._
import com.heimdali.repositories.{MemberRepository, _}
import com.heimdali.tasks.ProvisionResult
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

import scala.concurrent.ExecutionContext

class WorkspaceServiceImpl[F[_]](ldapClient: LDAPClient[F],
                                  yarnRepository: YarnRepository,
                                  hiveDatabaseRepository: HiveDatabaseRepository,
                                  ldapRepository: LDAPRepository,
                                  workspaceRepository: WorkspaceRequestRepository,
                                  complianceRepository: ComplianceRepository,
                                  approvalRepository: ApprovalRepository,
                                  transactor: Transactor[F],
                                  memberRepository: MemberRepository,
                                  appConfig: AppContext[F]
                                )(implicit val F: Effect[F], val executionContext: ExecutionContext)
  extends WorkspaceService[F]
    with LazyLogging {

  private val provisionContext =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

  private val GroupExtractor = "CN=edh_sw_([A-z0-9_]+),OU=.*".r

  def sharedMemberships(user: LDAPUser): List[String] =
    user.memberships.flatMap {
      case GroupExtractor(name) =>
        logger.info("found shared workspace {}", name)
        Some(name)
      case _ => None
    }.toList

  override def find(id: Long): OptionT[F, WorkspaceRequest] =
    OptionT {
      (for {
        workspace <- workspaceRepository.find(id).value
        datas <- hiveDatabaseRepository.findByWorkspace(id)
        yarns <- yarnRepository.findByWorkspace(id)
        approvals <- approvalRepository.findByWorkspaceId(id)
      } yield (workspace, datas, yarns, approvals))
        .transact(transactor)
        .map(
          r =>
            r._1.map(_.copy(data = r._2, processing = r._3, approvals = r._4))
        )
    }

  override def list(username: String): F[List[WorkspaceRequest]] =
    workspaceRepository.list(username).transact(transactor)

  def create(workspace: WorkspaceRequest): F[WorkspaceRequest] =
    (for {
      compliance <- complianceRepository.create(workspace.compliance)
      updatedWorkspace = workspace.copy(compliance = compliance)
      newWorkspace <- workspaceRepository.create(updatedWorkspace)

      insertedHive <- workspace.data.traverse[ConnectionIO, HiveDatabase] {
        db =>
          for {
            manager <- ldapRepository.create(db.managingGroup)
            _ <- memberRepository.create(workspace.requestedBy, manager.id.get)
            readonly <- db.readonlyGroup
              .map(ldapRepository.create)
              .sequence[ConnectionIO, LDAPRegistration]
            newHive <- hiveDatabaseRepository.create(
              db.copy(managingGroup = manager, readonlyGroup = readonly)
            )
            _ <- workspaceRepository.linkHive(
              newWorkspace.id.get,
              newHive.id.get
            )
          } yield
            newHive.copy(managingGroup = manager, readonlyGroup = readonly)
      }

      insertedYarn <- workspace.processing.traverse[ConnectionIO, Yarn] {
        yarn =>
          for {
            newYarn <- yarnRepository.create(yarn)
            _ <- workspaceRepository.linkYarn(
              newWorkspace.id.get,
              newYarn.id.get
            )
          } yield newYarn
      }
    } yield newWorkspace.copy(data = insertedHive, processing = insertedYarn))
      .transact(transactor)

  override def approve(id: Long, approval: Approval): F[Approval] =
    for {
      approval <- OptionT.liftF(approvalRepository.create(id, approval).transact(transactor)).value
      workspace <- find(id).value
      _ <- if (workspace.get.approvals.lengthCompare(2) == 0) OptionT.liftF(fs2.async.fork(provision(workspace.get))).value else OptionT.none(F).value
    } yield approval.get

  def provision(workspace: WorkspaceRequest): F[NonEmptyList[String]] = {
    import com.heimdali.tasks.ProvisionTask._

    val datas: List[Kleisli[F, AppContext[F], ProvisionResult]] = workspace.data.map(_.provision)
    val yarns: List[Kleisli[F, AppContext[F], ProvisionResult]] = workspace.processing.map(_.provision)

    (datas ++ yarns)
      .map(item => item(appConfig).map(_.messages))
      .toNel
      .get
      .sequence
      .map(_.flatten)
  }

}
