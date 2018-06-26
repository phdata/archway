package com.heimdali.services

import java.sql.Connection
import java.util.concurrent.Executors

import cats.data.OptionT
import cats.effect.Effect
import cats.implicits._
import com.heimdali.clients._
import com.heimdali.models._
import com.heimdali.repositories.{MemberRepository, _}
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

import scala.collection.immutable.Queue
import scala.concurrent.ExecutionContext

class ProvisionServiceImpl[F[_]](
    ldapClient: LDAPClient[F],
    hdfsClient: HDFSClient[F],
    hiveService: HiveClient[F],
    yarnClient: YarnClient[F],
    yarnRepository: YarnRepository,
    hiveDatabaseRepository: HiveDatabaseRepository,
    ldapRepository: LDAPRepository,
    connectionFactory: () => Connection,
    transactor: Transactor[F]
)(implicit val F: Effect[F]) extends ProvisionService[F] {

  def createDatabase(
      database: HiveDatabase,
      initialUser: String,
      elevate: Option[String]
  ): F[Unit] = {
    implicit val connection: Connection = connectionFactory()
    for {
      _ <- hdfsClient.createDirectory(database.location, elevate)
      _ <- hdfsClient.setQuota(database.location, database.sizeInGB)
      _ <- hiveService.createDatabase(database.name, database.location)

      _ <- createLDAP(database.managingGroup, database, initialUser)
      _ <- database.readonlyGroup
        .map(createLDAP(_, database, initialUser))
        .sequence

      _ <- hiveDatabaseRepository.complete(database.id.get).transact(transactor)
    } yield ()
  }

  def createLDAP(
      ldap: LDAPRegistration,
      database: HiveDatabase,
      requestedBy: String
  ): F[Unit] =
    for {
      _ <- ldapClient
        .createGroup(ldap.commonName, ldap.distinguishedName)
        .toOption
        .value
      _ <- ldapClient.addUser(ldap.commonName, requestedBy).value
      _ <- ldapRepository.complete(ldap.id.get).transact(transactor)
      _ <- hiveService.createRole(ldap.sentryRole)
      _ <- hiveService.grantGroup(ldap.commonName, ldap.sentryRole)
      _ <- hiveService.enableAccessToDB(database.name, ldap.sentryRole)
      _ <- hiveService.enableAccessToLocation(
        database.location,
        ldap.sentryRole
      )
    } yield ()

  def createYarn(yarn: Yarn): F[Unit] =
    for {
      _ <- yarnClient.createPool(yarn, Queue("root"))
      _ <- yarnRepository.complete(yarn.id.get).transact(transactor)
    } yield ()

  def provision(workspaceRequest: WorkspaceRequest): F[Unit] =
    for {
      _ <- workspaceRequest.data.traverse(
        createDatabase(
          _,
          workspaceRequest.requestedBy,
          if (workspaceRequest.singleUser) Some(workspaceRequest.requestedBy)
          else None
        )
      )
      _ <- workspaceRequest.processing.traverse(createYarn)
    } yield ()

}
