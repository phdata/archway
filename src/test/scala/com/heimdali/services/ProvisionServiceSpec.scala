package com.heimdali.services

import cats.data.{EitherT, OptionT}
import cats.effect.IO
import cats.syntax.applicative._
import com.heimdali.clients.{HDFSClient, HiveClient, LDAPClient, YarnClient, _}
import com.heimdali.models.WorkspaceMember
import com.heimdali.repositories.{MemberRepository, _}
import com.heimdali.test.fixtures._
import doobie._
import doobie.implicits._
import org.apache.hadoop.fs.Path
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class ProvisionServiceSpec
    extends FlatSpec
    with Matchers
    with MockFactory
    with DBTest {

  behavior of "Provision Service"

  ignore should "provision a workspace" in new Context {
    inSequence {
      hdfsClient.createDirectory _ expects (savedHive.location, None) returning IO
        .pure(new Path(savedHive.location))
      hdfsClient.setQuota _ expects (savedHive.location, savedHive.sizeInGB) returning IO
        .pure(HDFSAllocation(savedHive.location, savedHive.sizeInGB))
      hiveClient.createDatabase _ expects (savedHive.name, savedHive.location) returning IO.unit

      ldapClient.createGroup _ expects (savedLDAP.id.get, savedLDAP.commonName, savedLDAP.distinguishedName) returning EitherT
        .right(IO.unit)
      ldapClient.addUser _ expects (savedLDAP.distinguishedName, standardUsername) returning OptionT
        .some(LDAPUser("John Doe", standardUsername, Seq.empty))
      memberRepository.find _ expects (123, standardUsername) returning OptionT.some(WorkspaceMember(standardUsername, None, Some(123)))
      memberRepository.complete _ expects 123 returning 0.pure[ConnectionIO]
      ldapRepository.complete _ expects 123 returning savedLDAP
        .pure[ConnectionIO]
      hiveClient.createRole _ expects savedLDAP.sentryRole returning IO.unit
      hiveClient.grantGroup _ expects (savedLDAP.commonName, savedLDAP.sentryRole) returning IO.unit
      hiveClient.enableAccessToDB _ expects (savedHive.name, savedLDAP.sentryRole) returning IO.unit
      hiveClient.enableAccessToLocation _ expects (savedHive.location, savedLDAP.sentryRole) returning IO.unit
      hiveDatabaseRepository.complete _ expects savedHive.id.get returning 1
        .pure[ConnectionIO]

      yarnClient.createPool _ expects (poolName, maxCores, maxMemoryInGB) returning IO.unit
      yarnRepository.complete _ expects savedYarn.id.get returning 1
        .pure[ConnectionIO]
    }
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
  }
}
