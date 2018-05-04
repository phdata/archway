package com.heimdali.services

import akka.actor.{ActorRef, ActorSystem}
import com.heimdali.clients.{LDAPClient, LDAPUser}
import com.heimdali.models.SharedWorkspace
import com.heimdali.provisioning.WorkspaceProvisioner.Start
import com.heimdali.repositories.{ComplianceRepository, SharedWorkspaceRepository}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

trait WorkspaceService {
  def find(id: Long): Future[Option[SharedWorkspace]]

  def list(username: String): Future[Seq[SharedWorkspace]]

  def create(sharedWorkspace: SharedWorkspace): Future[SharedWorkspace]
}

class WorkspaceServiceImpl(ldapClient: LDAPClient,
                           workspaceRepository: SharedWorkspaceRepository,
                           complianceRepository: ComplianceRepository,
                           workspaceFactory: SharedWorkspace => ActorRef)
                          (implicit executionContext: ExecutionContext,
                           actorSystem: ActorSystem)
  extends WorkspaceService with LazyLogging {

  val GroupExtractor = "CN=edh_sw_([A-z0-9_]+),OU=.*".r

  def sharedMemberships(maybeUser: Option[LDAPUser]): Seq[String] =
    maybeUser.map { user =>
      logger.info("checking {}", user)
      user.memberships.flatMap {
        case GroupExtractor(name) =>
          logger.info("found shared workspace {}", name)
          Some(name)
        case _ => None
      }
    }.getOrElse(Seq.empty)


  override def find(id: Long): Future[Option[SharedWorkspace]] =
    workspaceRepository.find(id)

  override def list(username: String): Future[Seq[SharedWorkspace]] =
    for {
      user <- ldapClient.findUser(username)
      workspaces <- workspaceRepository.list(sharedMemberships(user))
    } yield workspaces

  override def create(sharedWorkspace: SharedWorkspace): Future[SharedWorkspace] =
    for {
      compliance <- complianceRepository.create(sharedWorkspace.compliance.get)
      created <- workspaceRepository.create(sharedWorkspace.copy(complianceId = compliance.id))
      _ <- Future(workspaceFactory(created) ! Start)
    } yield created
}