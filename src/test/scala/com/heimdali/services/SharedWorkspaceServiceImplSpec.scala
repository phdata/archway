package com.heimdali.services

import java.util.concurrent.LinkedBlockingDeque

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestActor.Message
import akka.testkit.{TestActor, TestActorRef, TestProbe}
import com.heimdali.clients.{LDAPClient, LDAPUser}
import com.heimdali.models.{LDAPRegistration, SharedWorkspace, WorkspaceMember}
import com.heimdali.repositories.{ComplianceRepository, SharedWorkspaceRepository}
import com.heimdali.test.fixtures._
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.TableFor2
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class SharedWorkspaceServiceImplSpec extends FlatSpec with Matchers with MockFactory {

  behavior of "ProjectServiceImpl"

  it should "list projects" in {
    val invalidWorkspace = "CN=non_workspace,OU=groups,dn=example,dn=com"
    val validWorkspace = "CN=edh_sw_project,OU=groups,dn=example,dn=com"
    val memberships = Seq(
      invalidWorkspace,
      validWorkspace
    )

    val client = mock[LDAPClient]
    client.findUser _ expects standardUsername returning Future {
      Some(LDAPUser("name", standardUsername, memberships))
    }

    val workspaceRepository = mock[SharedWorkspaceRepository]
    workspaceRepository.list _ expects Seq("project") returning Future {
      Seq(initialSharedWorkspace)
    }

    val complianceRepository = mock[ComplianceRepository]
    val factory = mockFunction[SharedWorkspace, ActorRef]

    val projectServiceImpl = new WorkspaceServiceImpl(client, workspaceRepository, complianceRepository, factory)
    val projects = Await.result(projectServiceImpl.list(standardUsername), 1 second)
      projects.length should be(1)
      projects.head should be(initialSharedWorkspace)
  }

  it should "get memberships accurately" in {
    val table: TableFor2[Option[LDAPUser], Seq[String]] = Table(
      ("user", "memberships"),
      (Some(LDAPUser("name", "username", Seq("something_else"))), Seq.empty)
    )

    forAll(table) { (user, memberships) =>
      val ldapClient = mock[LDAPClient]
      val workspaceRepo = mock[SharedWorkspaceRepository]
      val complianceRepository = mock[ComplianceRepository]
      val factory = mockFunction[SharedWorkspace, ActorRef]

      val projectServiceImpl = new WorkspaceServiceImpl(ldapClient, workspaceRepo, complianceRepository, factory)

      projectServiceImpl.sharedMemberships(user) should be(memberships)
    }
  }

  it should "create a workspace" in {
    val probe = TestProbe()

    val ldapClient = mock[LDAPClient]
    val complianceRepository = mock[ComplianceRepository]
    complianceRepository.create _ expects compliance returning Future(compliance.copy(id = Some(123)))
    val workspaceRepo = mock[SharedWorkspaceRepository]
    workspaceRepo.create _ expects initialSharedWorkspace.copy(complianceId = Some(123)) returning Future(initialSharedWorkspace.copy(complianceId = Some(123)))
    val factory = mockFunction[SharedWorkspace, ActorRef]
    factory expects initialSharedWorkspace.copy(complianceId = Some(123)) returning probe.ref

    val projectServiceImpl = new WorkspaceServiceImpl(ldapClient, workspaceRepo, complianceRepository, factory)

    val newWorkspace = Await.result(projectServiceImpl.create(initialSharedWorkspace), Duration.Inf)

    newWorkspace.complianceId shouldBe defined
  }

  it should "find a record" in {
    val ldapClient = mock[LDAPClient]
    val complianceRepository = mock[ComplianceRepository]
    val factory = mockFunction[SharedWorkspace, ActorRef]

    val workspaceRepo = mock[SharedWorkspaceRepository]
    workspaceRepo.find _ expects id returning Future(Some(initialSharedWorkspace))

    val projectServiceImpl = new WorkspaceServiceImpl(ldapClient, workspaceRepo, complianceRepository, factory)
    val foundWorkspace = Await.result(projectServiceImpl.find(id), Duration.Inf)

    foundWorkspace shouldBe defined
  }

  it should "list members" in {
    val ldapClient = mock[LDAPClient]
    ldapClient.groupMembers _ expects ldapDn returning Future(Seq(LDAPUser("John Doe", "johndoe", Seq.empty)))
    val complianceRepository = mock[ComplianceRepository]
    val factory = mockFunction[SharedWorkspace, ActorRef]
    val workspaceRepo = mock[SharedWorkspaceRepository]
    workspaceRepo.find _ expects id returning Future(Some(initialSharedWorkspace.copy(ldap = Some(ldap))))

    val projectServiceImpl = new WorkspaceServiceImpl(ldapClient, workspaceRepo, complianceRepository, factory)
    val members = Await.result(projectServiceImpl.members(id), Duration.Inf)

    members shouldBe Seq(WorkspaceMember("johndoe", "John Doe"))
  }

  implicit val system: ActorSystem = ActorSystem()

  def factory(project: SharedWorkspace): ActorRef =
    TestActorRef.create(system, Props(classOf[TestActor], new LinkedBlockingDeque[Message]()))

}
