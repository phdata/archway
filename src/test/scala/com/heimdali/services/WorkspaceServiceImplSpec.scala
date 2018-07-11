package com.heimdali.services

import java.time.Instant

import cats.data.OptionT
import cats.effect.IO
import cats.syntax.applicative._
import com.heimdali.clients._
import com.heimdali.repositories.{MemberRepository, _}
import com.heimdali.test.fixtures._
import doobie._
import doobie.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.TableFor2
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class WorkspaceServiceImplSpec
    extends FlatSpec
    with Matchers
    with MockFactory
    with DBTest {

  behavior of "Workspace Service"

  it should "list projects" in new Context {
    val invalidWorkspace = "CN=non_workspace,OU=groups,dn=example,dn=com"
    val validWorkspace = "CN=edh_sw_project,OU=groups,dn=example,dn=com"
    val memberships = Seq(
      invalidWorkspace,
      validWorkspace
    )

    workspaceRepository.list _ expects standardUsername returning List(
      savedWorkspaceRequest
    ).pure[ConnectionIO]

    val projects = projectServiceImpl.list(standardUsername).unsafeRunSync()
    projects.length should be(1)
    projects.head should be(savedWorkspaceRequest)
  }

  it should "get memberships accurately" in new Context {
    val table: TableFor2[LDAPUser, Seq[String]] = Table(
      ("user", "memberships"),
      (LDAPUser("name", "username", Seq("something_else")), Seq.empty)
    )

    forAll(table) { (user, memberships) =>
      projectServiceImpl.sharedMemberships(user) should be(memberships)
    }
  }

  it should "create a workspace" in new Context {
    inSequence {
      (complianceRepository.create _).expects(initialCompliance).returning(savedCompliance.pure[ConnectionIO])
      (workspaceRepository.create _).expects(initialWorkspaceRequest.copy(compliance = savedCompliance)).returning(initialWorkspaceRequest.copy(id = Some(id), compliance = savedCompliance).pure[ConnectionIO])
      (ldapRepository.create _).expects(initialLDAP).returning(savedLDAP.pure[ConnectionIO])
      (memberRepository.create _).expects(standardUsername, id).returning(1L.pure[ConnectionIO])
      (hiveDatabaseRepository.create _).expects(initialHive.copy(managingGroup = savedLDAP)).returning(savedHive.pure[ConnectionIO])
      (workspaceRepository.linkHive _).expects(id, id).returning(1.pure[ConnectionIO])
      (yarnRepository.create _).expects(initialYarn).returning(savedYarn.pure[ConnectionIO])
      (workspaceRepository.linkYarn _).expects(id, id).returning(1.pure[ConnectionIO])
    }

    val newWorkspace =
      projectServiceImpl.create(initialWorkspaceRequest).unsafeRunSync()

    newWorkspace.processing should not be empty
    newWorkspace.data should not be empty
  }

  it should "find a record" in new Context {
    workspaceRepository.find _ expects id returning OptionT.some(
      savedWorkspaceRequest
    )
    hiveDatabaseRepository.findByWorkspace _ expects id returning List(
      savedHive
    ).pure[ConnectionIO]
    yarnRepository.findByWorkspace _ expects id returning List(savedYarn)
      .pure[ConnectionIO]
    approvalRepository.findByWorkspaceId _ expects id returning List(approval())
      .pure[ConnectionIO]

    val foundWorkspace = projectServiceImpl.find(id).value.unsafeRunSync()

    foundWorkspace shouldBe defined
    foundWorkspace.get.data should not be empty
    foundWorkspace.get.processing should not be empty
  }

  it should "approve the workspace" in new Context {
    val instant = Instant.now()
    approvalRepository.create _ expects (id, approval(instant)) returning approval(
      instant
    ).copy(id = Some(id)).pure[ConnectionIO]

    projectServiceImpl.approve(id, approval(instant))
  }

  trait Context {
    val ldapClient: LDAPClient[IO] = mock[LDAPClient[IO]]

    val workspaceRepository: WorkspaceRequestRepository =
      mock[WorkspaceRequestRepository]
    val complianceRepository: ComplianceRepository = mock[ComplianceRepository]
    val yarnRepository: YarnRepository = mock[YarnRepository]
    val hiveDatabaseRepository: HiveDatabaseRepository =
      mock[HiveDatabaseRepository]
    val ldapRepository: LDAPRepository = mock[LDAPRepository]
    val approvalRepository: ApprovalRepository = mock[ApprovalRepository]
    val contextProvider: LoginContextProvider = mock[LoginContextProvider]
    val memberRepository: MemberRepository = mock[MemberRepository]

    def projectServiceImpl =
      new WorkspaceServiceImpl[IO](
        ldapClient,
        yarnRepository,
        hiveDatabaseRepository,
        ldapRepository,
        workspaceRepository,
        complianceRepository,
        approvalRepository,
        transactor,
        memberRepository,
        null
      )
  }

}
