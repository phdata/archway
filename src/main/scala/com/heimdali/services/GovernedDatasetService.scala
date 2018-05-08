package com.heimdali.services

import akka.actor.ActorRef
import akka.http.scaladsl.server.directives.OnSuccessMagnet
import akka.pattern.ask
import akka.util.Timeout
import com.heimdali.clients.LDAPClient
import com.heimdali.models.Dataset._
import com.heimdali.models._
import com.heimdali.provisioning.WorkspaceProvisioner.Start
import com.heimdali.repositories.{ComplianceRepository, DatasetRepository, GovernedDatasetRepository}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

trait GovernedDatasetService {
  def members(id: Long, dataset: String): Future[Seq[WorkspaceMember]]

  def addMember(id: Long, dataset: String, username: String): Future[WorkspaceMember]

  def removeMember(id: Long, dataset: String, username: String): Future[WorkspaceMember]

  def create(governedDataset: GovernedDataset): Future[GovernedDataset]

  def find(username: String): Future[Seq[GovernedDataset]]

  def get(id: Long): Future[Option[GovernedDataset]]

}


class GovernedDatasetServiceImpl(governedDatasetRepository: GovernedDatasetRepository,
                                 datasetRepository: DatasetRepository,
                                 complianceRepository: ComplianceRepository,
                                 ldapClient: LDAPClient,
                                 provisionFactory: Dataset => ActorRef)
                                (implicit executionContext: ExecutionContext)
  extends GovernedDatasetService with LazyLogging {
  implicit val timeout: Timeout = Timeout(1 second)

  val GroupExtractor = "CN=edh_raw_([A-z0-9_]+),OU=.*".r

  def filter(memberships: Seq[String]): Seq[String] =
    memberships.flatMap {
      case GroupExtractor(name) =>
        logger.info("found dataset {}", name)
        Some(name)
      case name =>
        logger.info("ignoring group {}", name)
        None
    }.distinct

  override def find(username: String): Future[Seq[GovernedDataset]] =
    for {
      user <- ldapClient.findUser(username)
      memberships <- Future(filter(user.get.memberships))
      datasets <- governedDatasetRepository.find(memberships)
    } yield datasets

  override def create(governedDataset: GovernedDataset): Future[GovernedDataset] =
    for {
      raw <- datasetRepository.create(Dataset(RawDataset, governedDataset.systemName, governedDataset.createdBy.get))
      staging <- datasetRepository.create(Dataset(StagingDataset, governedDataset.systemName, governedDataset.createdBy.get))
      modeled <- datasetRepository.create(Dataset(ModeledDataset, governedDataset.systemName, governedDataset.createdBy.get))
      compliance <- complianceRepository.create(governedDataset.compliance.get)
      readyDataset <- Future(governedDataset.copy(rawDatasetId = raw.id, stagingDatasetId = staging.id, modeledDatasetId = modeled.id, complianceId = compliance.id))
      dataset <- governedDatasetRepository.create(readyDataset)
      _ <- Future.traverse(Seq(raw, staging, modeled))(ds => Future(provisionFactory(ds) ! Start))
    } yield dataset

  //TODO: Add testing around all single item gets
  override def get(id: Long): Future[Option[GovernedDataset]] =
    governedDatasetRepository.get(id)

  override def members(id: Long, dataset: String): Future[Seq[WorkspaceMember]] =
    for {
      workspace <- datasetRepository.find(id, dataset)
      members <- ldapClient.groupMembers(workspace.get.ldap.get.distinguishedName)
    } yield members.map(m => WorkspaceMember(m.username, m.name))

  override def addMember(id: Long, dataset: String, username: String): Future[WorkspaceMember] =
    for {
      workspace <- datasetRepository.find(id, dataset)
      member <- ldapClient.addUser(workspace.get.ldap.get.commonName, username)
    } yield WorkspaceMember(member.username, member.name)

  override def removeMember(id: Long, dataset: String, username: String): Future[WorkspaceMember] =
    for {
      workspace <- datasetRepository.find(id, dataset)
      member <- ldapClient.removeUser(workspace.get.ldap.get.commonName, username)
    } yield WorkspaceMember(member.username, member.name)
}