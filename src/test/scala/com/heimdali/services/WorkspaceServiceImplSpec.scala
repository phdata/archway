package com.heimdali.services

import com.heimdali.repositories.MemberRepository
import java.time.Instant

import cats.data.{EitherT, OptionT}
import cats.effect.IO
import cats.syntax.applicative._
import com.heimdali.clients._
import com.heimdali.models.WorkspaceMember
import com.heimdali.repositories._
import com.heimdali.test.fixtures._
import doobie._
import doobie.implicits._
import org.apache.hadoop.fs.Path
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.TableFor2
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.Queue
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

    ldapClient.findUser _ expects standardUsername returning OptionT.some(
      LDAPUser("name", standardUsername, memberships)
    )
    workspaceRepository.list _ expects List("project") returning List(
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
      complianceRepository.create _ expects initialCompliance returning savedCompliance
        .pure[ConnectionIO]
      workspaceRepository.create _ expects initialWorkspaceRequest.copy(
        compliance = savedCompliance
      ) returning initialWorkspaceRequest
        .copy(id = Some(id), compliance = savedCompliance)
        .pure[ConnectionIO]
      ldapRepository.create _ expects initialLDAP returning savedLDAP
        .pure[ConnectionIO]
      hiveDatabaseRepository.create _ expects initialHive.copy(
        managingGroup = savedLDAP
      ) returning savedHive.pure[ConnectionIO]
      workspaceRepository.linkHive _ expects (id, id) returning 1
        .pure[ConnectionIO]
      yarnRepository.create _ expects initialYarn returning savedYarn
        .pure[ConnectionIO]
      workspaceRepository.linkYarn _ expects (id, id) returning 1
        .pure[ConnectionIO]
    }

    val newWorkspace =
      projectServiceImpl.create(initialWorkspaceRequest).unsafeRunSync()

    newWorkspace.processing should not be empty
    newWorkspace.data should not be empty
  }

  it should "provision a workspace" in new Context {
    inSequence {
      hdfsClient.createDirectory _ expects (savedHive.location, None) returning IO
        .pure(new Path(savedHive.location))
      hdfsClient.setQuota _ expects (savedHive.location, savedHive.sizeInGB) returning IO
        .pure(HDFSAllocation(savedHive.location, savedHive.sizeInGB))
      hiveClient.createDatabase _ expects (savedHive.name, savedHive.location) returning IO.unit

      ldapClient.createGroup _ expects (savedLDAP.commonName, savedLDAP.distinguishedName) returning EitherT
        .right(IO.unit)
      ldapClient.addUser _ expects (savedLDAP.commonName, standardUsername) returning OptionT
        .some(LDAPUser("John Doe", standardUsername, Seq.empty))
      ldapRepository.complete _ expects 123 returning savedLDAP
        .pure[ConnectionIO]
      hiveClient.createRole _ expects savedLDAP.sentryRole returning IO.unit
      hiveClient.grantGroup _ expects (savedLDAP.commonName, savedLDAP.sentryRole) returning IO.unit
      hiveClient.enableAccessToDB _ expects (savedHive.name, savedLDAP.sentryRole) returning IO.unit
      hiveClient.enableAccessToLocation _ expects (savedHive.location, savedLDAP.sentryRole) returning IO.unit
      hiveDatabaseRepository.complete _ expects savedHive.id.get returning 1
        .pure[ConnectionIO]

      yarnClient.createPool _ expects (savedYarn, Queue("root")) returning IO.unit
      yarnRepository.complete _ expects savedYarn.id.get returning 1
        .pure[ConnectionIO]
    }

    val newWorkspace =
      projectServiceImpl.provision(savedWorkspaceRequest).unsafeRunSync()
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

  it should "list members" in new Context {
    memberRepository.findByDatabase _ expects ("sesame", Manager) returning List(WorkspaceMember(standardUsername, None, None)).pure[ConnectionIO]

    val members =
      projectServiceImpl.members(id, "sesame", Manager).unsafeRunSync()

    members shouldBe Seq(WorkspaceMember(standardUsername, None))
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
    val hdfsClient: HDFSClient[IO] = mock[HDFSClient[IO]]
    val hiveClient: HiveClient[IO] = mock[HiveClient[IO]]
    val yarnClient: YarnClient[IO] = mock[YarnClient[IO]]

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
        hdfsClient,
        hiveClient,
        yarnClient,
        yarnRepository,
        hiveDatabaseRepository,
        ldapRepository,
        workspaceRepository,
        complianceRepository,
        () => null,
        approvalRepository,
        transactor,
        contextProvider,
        memberRepository
      )
  }

}
