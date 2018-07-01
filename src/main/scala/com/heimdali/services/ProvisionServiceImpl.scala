package com.heimdali.services

import com.typesafe.scalalogging.LazyLogging
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
import treelog.LogTreeSyntaxWithoutAnnotations._

class ProvisionServiceImpl[F[_]](
    ldapClient: LDAPClient[F],
    hdfsClient: HDFSClient[F],
    hiveService: HiveClient[F],
    yarnClient: YarnClient[F],
    yarnRepository: YarnRepository,
    hiveDatabaseRepository: HiveDatabaseRepository,
    ldapRepository: LDAPRepository,
    memberRepository: MemberRepository,
    transactor: Transactor[F]
)(implicit val F: Effect[F]) extends ProvisionService[F] with LazyLogging {

  def createDatabase(
      database: HiveDatabase,
      initialUser: String,
      elevate: Option[String]
  ): F[Unit] = {
    implicit val connection: Connection = connectionFactory()
    for {
      _ <- F.pure(logger.info(s"creating directory ${database.location}"))
      _ <- hdfsClient.createDirectory(database.location, elevate)
      _ <- F.pure(logger.info(s"setting a ${database.sizeInGB} quota on ${database.location}"))
      _ <- hdfsClient.setQuota(database.location, database.sizeInGB)
      _ <- F.pure(logger.info(s"creating database ${database.name} at ${database.location}"))
      _ <- hiveService.createDatabase(database.name, database.location)

      _ <- createLDAP(database.managingGroup, database, initialUser)
      _ <- database.readonlyGroup
        .map(createLDAP(_, database, initialUser))
        .sequence

      _ <- F.pure(logger.info(s"marking ${database.name} complete"))
      _ <- hiveDatabaseRepository.complete(database.id.get).transact(transactor)
    } yield ()
  }

  def createLDAP(
      ldap: LDAPRegistration,
      database: HiveDatabase,
      requestedBy: String
  ): F[Unit] =
    for {
      _ <- F.pure(logger.info(s"creating ${ldap.commonName} group at ${ldap.distinguishedName}"))
      _ <- ldapClient
        .createGroup(ldap.id.get, ldap.commonName, ldap.distinguishedName)
        .toOption
      .value
      _ <- logger.info(s"adding requester ${requestedBy} to ${ldap.commonName}").pure[F]
      _ <- ldapClient.addUser(ldap.distinguishedName, requestedBy).value
      member <- memberRepository.find(ldap.id.get, requestedBy).value.transact(transactor)
      _ <- memberRepository.complete(member.get.id.get).transact(transactor)
      _ <- logger.info(s"marking ${ldap.commonName} as complete").pure[F]
      _ <- ldapRepository.complete(ldap.id.get).transact(transactor)
      _ <- logger.info(s"creating role called ${ldap.sentryRole}").pure[F]
        _ <- hiveService.createRole(ldap.sentryRole)
      _ <- logger.info(s"granting role ${ldap.sentryRole} to ${ldap.commonName}").pure[F]
        _ <- hiveService.grantGroup(ldap.commonName, ldap.sentryRole)
      _ <- logger.info(s"granting role ${ldap.sentryRole} access to database ${database.name}").pure[F]
        _ <- hiveService.enableAccessToDB(database.name, ldap.sentryRole)
      _ <- logger.info(s"granting role ${ldap.sentryRole} access to HDFS location ${database.location}").pure[F]
        _ <- hiveService.enableAccessToLocation(
          database.location,
          ldap.sentryRole
        )
    } yield ()

  def createYarn(yarn: Yarn): F[Unit] =
    for {
      _ <- logger.info(s"creating ${yarn.poolName} yarn pool").pure[F]
      _ <- yarnClient.createPool(yarn, Queue("root"))
      _ <- logger.info(s"marking ${yarn.poolName} yarn pool complete").pure[F]
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
//      _ <- workspaceRequest.processing.traverse(createYarn)
    } yield ()

}
