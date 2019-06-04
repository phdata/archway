package com.heimdali.services

import java.util.concurrent.TimeUnit

import cats.data.OptionT
import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.clients._
import com.heimdali.models._
import com.heimdali.provisioning.DefaultProvisioningService
import com.heimdali.test.fixtures._
import doobie._
import doobie.implicits._
import org.apache.hadoop.fs.Path
import org.scalamock.scalatest.MockFactory
import scala.concurrent.duration._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class DefaultProvisioningServiceSpec
  extends FlatSpec
    with MockFactory
    with Matchers
    with AppContextProvider {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  behavior of "DefaultProvisioningServiceSpec"

  it should "fire and forget" in new Context {
    // make sure we actually call provisioning code, but take "too long"
    (context.hdfsClient.createDirectory _)
      .expects(savedHive.location, None)
      .returning(
        for {
          _ <- IO(println("I still run though"))
          _ <- actualTimer.sleep(2 second)
          path <- IO(new Path(savedHive.location))
        } yield path
      )

    val actual: IO[Long] = for {
      start <- actualTimer.clock.realTime(TimeUnit.MILLISECONDS)
      _ <- provisioningService.attemptProvision(savedWorkspaceRequest, 0)
      end <- actualTimer.clock.realTime(TimeUnit.MILLISECONDS)
      _ <- actualTimer.sleep(1 second)
    } yield end - start

    actual.unsafeRunSync() should be < 2000L
  }

  it should "allow more than required approvals" in new Context {
    // make sure we actually call provisioning code, but take "too long"
    (context.hdfsClient.createDirectory _)
      .expects(savedHive.location, None)
      .returning(
        for {
          _ <- actualTimer.sleep(1 second)
          path <- IO(new Path(savedHive.location))
        } yield path
      )
    val singleApprovalWorkspace = savedWorkspaceRequest.copy(approvals = List(approval(testTimer.instant)))

    (for {
      fabric <- provisioningService.attemptProvision(singleApprovalWorkspace, 0)
      _ <- actualTimer.sleep(100 millis)
      _ <- fabric.cancel
      _ <- actualTimer.sleep(100 millis)
    } yield ()).unsafeRunSync()
  }

  it should "provision a workspace" in new Context {
    inSequence {
      inSequence {
        context.hdfsClient.createDirectory _ expects(savedHive.location, None) returning IO
          .pure(new Path(savedHive.location))
        context.databaseRepository.directoryCreated _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
        context.hdfsClient.setQuota _ expects(savedHive.location, savedHive.sizeInGB) returning IO
          .pure(HDFSAllocation(savedHive.location, savedHive.sizeInGB))
        context.databaseRepository.quotaSet _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
        context.workspaceRequestRepository.find _ expects id returning OptionT.some(savedWorkspaceRequest)
        context.hiveClient.createDatabase _ expects(savedHive.name, savedHive.location, "Sesame", Map("phi_data" -> "false", "pci_data" -> "false", "pii_data" -> "false")) returning IO.unit
        context.databaseRepository.databaseCreated _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]

        context.provisioningLDAPClient.createGroup _ expects(savedLDAP.commonName, *) returning IO.unit
        context.ldapRepository.groupCreated _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
        context.sentryClient.createRole _ expects savedLDAP.sentryRole returning IO.unit
        context.ldapRepository.roleCreated _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
        context.sentryClient.grantGroup _ expects(savedLDAP.commonName, savedLDAP.sentryRole) returning IO.unit
        context.ldapRepository.groupAssociated _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
        context.sentryClient.enableAccessToDB _ expects(savedHive.name, savedLDAP.sentryRole, Manager) returning IO.unit
        context.databaseGrantRepository.databaseGranted _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
        context.sentryClient.enableAccessToLocation _ expects(savedHive.location, savedLDAP.sentryRole) returning IO.unit
        context.databaseGrantRepository.locationGranted _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]

        context.provisioningLDAPClient.createGroup _ expects(savedLDAP.commonName, *) returning IO.unit
        context.ldapRepository.groupCreated _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
        context.sentryClient.createRole _ expects savedLDAP.sentryRole returning IO.unit
        context.ldapRepository.roleCreated _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
        context.sentryClient.grantGroup _ expects(savedLDAP.commonName, savedLDAP.sentryRole) returning IO.unit
        context.ldapRepository.groupAssociated _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
        context.sentryClient.enableAccessToDB _ expects(savedHive.name, savedLDAP.sentryRole, ReadOnly) returning IO.unit
        context.databaseGrantRepository.databaseGranted _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
        context.sentryClient.enableAccessToLocation _ expects(savedHive.location, savedLDAP.sentryRole) returning IO.unit
        context.databaseGrantRepository.locationGranted _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
      }

      inSequence {
        context.provisioningLDAPClient.addUser _ expects(savedLDAP.distinguishedName, standardUserDN) returning OptionT.some(standardUserDN)
        context.memberRepository.complete _ expects(id, standardUserDN) returning 0.pure[ConnectionIO]
      }

      inSequence {
        context.yarnClient.createPool _ expects(poolName, maxCores, maxMemoryInGB) returning IO.unit
        context.yarnRepository.complete _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
      }

      inSequence {
        context.provisioningLDAPClient.createGroup _ expects(savedLDAP.commonName, *) returning IO.unit
        context.ldapRepository.groupCreated _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
        context.sentryClient.createRole _ expects savedLDAP.sentryRole returning IO.unit
        context.ldapRepository.roleCreated _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
        context.sentryClient.grantGroup _ expects(savedLDAP.commonName, savedLDAP.sentryRole) returning IO.unit
        context.ldapRepository.groupAssociated _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
        context.sentryClient.grantPrivilege _ expects(*, *, *) returning IO.unit
        context.applicationRepository.consumerGroupAccess _ expects(id, testTimer.instant) returning 0.pure[ConnectionIO]
      }

      inSequence {
        context.provisioningLDAPClient.addUser _ expects(savedLDAP.distinguishedName, standardUserDN) returning OptionT.some(standardUserDN)
        context.memberRepository.complete _ expects(id, standardUserDN) returning 0.pure[ConnectionIO]
      }

      context.workspaceRequestRepository.markProvisioned _ expects(id, *) returning 0.pure[ConnectionIO]
    }

    provisioningService
      .attemptProvision(savedWorkspaceRequest, 0)
      .unsafeRunSync()
      .join
      .unsafeRunSync()
      .map(x => println(x.message))
  }

  trait Context {
    implicit val timer: Timer[IO] = testTimer
    val actualTimer = IO.timer(ExecutionContext.global)

    val context: AppContext[IO] = genMockContext()

    lazy val provisioningService = new DefaultProvisioningService[IO](context, ExecutionContext.global)
  }

}
