package com.heimdali.services

import cats.effect.IO
import com.heimdali.clients.{HDFSClient, HiveClient, LDAPClient, YarnClient}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
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

class ProvisionServiceSpec
    extends FlatSpec
    with Matchers
    with MockFactory
    with DBTest {

  behavior of "Provision Service"

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
      provisionService.provision(savedWorkspaceRequest).unsafeRunSync()
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

    def provisionService =
      new ProvisionServiceImpl(
        ldapClient,
        hdfsClient,
        hiveClient,
        yarnClient,
        yarnRepository,
        hiveDatabaseRepository,
        ldapRepository,
        () => null,
        transactor
      )
  }
}
