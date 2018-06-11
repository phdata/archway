package com.heimdali.services

import java.sql.Connection

import cats.data.OptionT
import cats.effect.{Async, Concurrent, Effect, IO}
import cats.implicits._
import com.heimdali.clients._
import com.heimdali.models._
import com.heimdali.repositories._
import com.typesafe.scalalogging.LazyLogging
import cats.effect.implicits._
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

import scala.collection.immutable.Queue
import scala.concurrent.ExecutionContext

trait WorkspaceService[F[_]] {

  def find(id: Long): OptionT[F, WorkspaceRequest]

  def list(username: String): F[List[WorkspaceRequest]]

  def create(workspace: WorkspaceRequest): F[WorkspaceRequest]

  def provision(workspace: WorkspaceRequest): F[Unit]

  def members[A <: DatabaseRole](id: Long, databaseName: String, roleName: A): F[List[WorkspaceMember]]

  def addMember[A <: DatabaseRole](id: Long, databaseName: String, roleName: A, username: String): OptionT[F, WorkspaceMember]

  def removeMember[A <: DatabaseRole](id: Long, databaseName: String, roleName: A, username: String): OptionT[F, WorkspaceMember]

  def approve[A <: ApproverRole](id: Long, approverRole: A): F[Approval]

}

class WorkspaceServiceImpl[F[_] : Effect](ldapClient: LDAPClient[F],
                                          hdfsClient: HDFSClient[F],
                                          hiveService: HiveClient[F],
                                          yarnClient: YarnClient[F],
                                          yarnRepository: YarnRepository,
                                          hiveDatabaseRepository: HiveDatabaseRepository,
                                          ldapRepository: LDAPRepository,
                                          workspaceRepository: WorkspaceRequestRepository,
                                          complianceRepository: ComplianceRepository,
                                          connectionFactory: () => Connection,
                                          transactor: Transactor[F])
                                         (implicit val executionContext: ExecutionContext)
  extends WorkspaceService[F]
    with LazyLogging {

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
      } yield (workspace, datas, yarns))
        .transact(transactor)
        .map(r => r._1.map(_.copy(data = r._2, processing = r._3)))
    }

  override def list(username: String): F[List[WorkspaceRequest]] =
    ldapClient.findUser(username).semiflatMap { user =>
      val memberships = sharedMemberships(user)
      workspaceRepository.list(memberships).transact(transactor)
    }.getOrElse(List.empty)

  def createDatabase(database: HiveDatabase, initialUser: String, elevate: Option[String]): F[Unit] = {
    implicit val connection: Connection = connectionFactory()
    for {
      _ <- hdfsClient.createDirectory(database.location, elevate)
      _ <- hdfsClient.setQuota(database.location, database.sizeInGB)
      _ <- hiveService.createDatabase(database.name, database.location)

      _ <- createLDAP(database.managingGroup, database, initialUser)
      _ <- database.readonlyGroup.map(createLDAP(_, database, initialUser)).sequence

      _ <- hiveDatabaseRepository.complete(database.id.get).transact(transactor)
    } yield ()
  }

  def createLDAP(ldap: LDAPRegistration, database: HiveDatabase, requestedBy: String): F[Unit] =
    for {
      _ <- ldapClient.createGroup(ldap.commonName, ldap.distinguishedName).toOption.value
      _ <- ldapClient.addUser(ldap.commonName, requestedBy).value
      _ <- ldapRepository.complete(ldap.id.get).transact(transactor)
      _ <- hiveService.createRole(ldap.sentryRole)
      _ <- hiveService.grantGroup(ldap.commonName, ldap.sentryRole)
      _ <- hiveService.enableAccessToDB(database.name, ldap.sentryRole)
      _ <- hiveService.enableAccessToLocation(database.location, ldap.sentryRole)
    } yield ()

  def createYarn(yarn: Yarn): F[Unit] =
    for {
      _ <- yarnClient.createPool(yarn, Queue("root"))
      _ <- yarnRepository.complete(yarn.id.get).transact(transactor)
    } yield ()

  def create(workspace: WorkspaceRequest): F[WorkspaceRequest] =
    (for {
      compliance <- complianceRepository.create(workspace.compliance)
      updatedWorkspace = workspace.copy(compliance = compliance)
      newWorkspace <- workspaceRepository.create(updatedWorkspace)

      insertedHive <- workspace.data.traverse[ConnectionIO, HiveDatabase] { db =>
        for {
          manager <- ldapRepository.create(db.managingGroup)
          readonly <- db.readonlyGroup.map(ldapRepository.create).sequence[ConnectionIO, LDAPRegistration]
          newHive <- hiveDatabaseRepository.create(db.copy(managingGroup = manager, readonlyGroup = readonly))
          _ <- workspaceRepository.linkHive(newWorkspace.id.get, newHive.id.get)
        } yield newHive.copy(managingGroup = manager, readonlyGroup = readonly)
      }

      insertedYarn <- workspace.processing.traverse[ConnectionIO, Yarn] { yarn =>
        for {
          newYarn <- yarnRepository.create(yarn)
          _ <- workspaceRepository.linkYarn(newWorkspace.id.get, newYarn.id.get)
        } yield newYarn
      }
    } yield newWorkspace.copy(data = insertedHive, processing = insertedYarn)).transact(transactor)

  def provision(workspaceRequest: WorkspaceRequest): F[Unit] =
    for {
      _ <- workspaceRequest.data.traverse(createDatabase(_, workspaceRequest.requestedBy, if (workspaceRequest.singleUser) Some(workspaceRequest.requestedBy) else None))
      _ <- workspaceRequest.processing.traverse(createYarn)
    } yield ()

  def members[A <: DatabaseRole](id: Long, databaseName: String, roleName: A): F[List[WorkspaceMember]] =
    ldapRepository.find(id, databaseName, roleName).value.transact(transactor).flatMap {
      case Some(ldap) => ldapClient.groupMembers(ldap.distinguishedName).map(w => w.map { m => WorkspaceMember(m.username, m.name) })
      case _ => Effect[F].pure(List.empty)
    }

  def addMember[A <: DatabaseRole](id: Long, databaseName: String, roleName: A, username: String): OptionT[F, WorkspaceMember] =
    for {
      ldap <- OptionT(ldapRepository.find(id, databaseName, roleName).value.transact(transactor))
      member <- ldapClient.addUser(ldap.commonName, username).map(member => WorkspaceMember(member.username, member.name))
    } yield member

  def removeMember[A <: DatabaseRole](id: Long, databaseName: String, roleName: A, username: String): OptionT[F, WorkspaceMember] =
    for {
      ldap <- OptionT(ldapRepository.find(id, databaseName, roleName).value.transact(transactor))
      member <- ldapClient.removeUser(ldap.commonName, username).map(member => WorkspaceMember(member.username, member.name))
    } yield member
}