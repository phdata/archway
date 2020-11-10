package io.phdata.services

import java.util.concurrent.TimeUnit

import cats.data.OptionT
import cats.effect.{IO, Timer}
import cats.implicits._
import io.phdata.AppContext
import io.phdata.clients._
import io.phdata.models._
import io.phdata.provisioning.DefaultProvisioningService
import io.phdata.test.fixtures._
import doobie._
import doobie.implicits._
import org.apache.hadoop.fs.Path
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class DefaultProvisioningServiceSpec
  extends FlatSpec
    with MockFactory
    with Matchers
    with AppContextProvider {

  behavior of "DefaultProvisioningServiceSpec"

  it should "fire and forget" in new Context {
    // make sure we actually call provisioning code, but take "too long"
    context.lookupLDAPClient.findUserByDN _ expects (standardUserDN) returning OptionT.some(ldapUser)
    (context.hdfsClient.createUserDirectory _)
      .expects(standardUsername)
      .returning(
        for {
          _ <- IO(println("I still run though"))
          _ <- timer.sleep(2 second)
          path <- IO(new Path(savedHive.location))
        } yield path
      )

    val actual: IO[Long] = for {
      start <- timer.clock.realTime(TimeUnit.MILLISECONDS)
      _ <- provisioningService.attemptProvision(savedWorkspaceRequest, 0)
      end <- timer.clock.realTime(TimeUnit.MILLISECONDS)
      _ <- timer.sleep(1 second)
    } yield end - start

    actual.unsafeRunSync() should be < 2000L
  }

  it should "allow more than required approvals" in new Context {
    // make sure we actually call provisioning code, but take "too long"
    context.lookupLDAPClient.findUserByDN _ expects (standardUserDN) returning OptionT.some(ldapUser)
    (context.hdfsClient.createUserDirectory _)
      .expects(standardUsername)
      .returning(
        for {
          _ <- timer.sleep(1 second)
          path <- IO(new Path(savedHive.location))
        } yield path
      )
    val singleApprovalWorkspace = savedWorkspaceRequest.copy(approvals = List(approval(testTimer.instant)))

    (for {
      fabric <- provisioningService.attemptProvision(singleApprovalWorkspace, 0)
      _ <- timer.sleep(100 millis)
      _ <- fabric.cancel
      _ <- timer.sleep(100 millis)
    } yield ()).unsafeRunSync()
  }

  it should "provision a workspace" in new Context {
    inSequence {
      inSequence {
        context.lookupLDAPClient.findUserByDN _ expects (standardUserDN) returning OptionT.some(ldapUser)
        (context.hdfsClient.createUserDirectory _)
          .expects(standardUsername) returning new Path(standardUsername).pure[IO]
        context.hdfsClient.createHiveDirectory _ expects(savedHive.location) returning IO
          .pure(new Path(savedHive.location))
        context.databaseRepository.directoryCreated _ expects(id, *) returning 0.pure[ConnectionIO]
        context.hdfsService.setQuota _ expects(savedHive.location, savedHive.sizeInGB, id, *) returning IO.unit
        context.workspaceRequestRepository.find _ expects id returning OptionT.some(savedWorkspaceRequest)
        context.hiveClient.createDatabase _ expects(savedHive.name, savedHive.location, "Sesame", Map("phi_data" -> "false", "pci_data" -> "false", "pii_data" -> "false")) returning IO.unit
        context.databaseRepository.databaseCreated _ expects(id, *) returning 0.pure[ConnectionIO]

        context.provisioningLDAPClient.createGroup _ expects(savedLDAP.commonName, *) returning IO.unit
        context.ldapRepository.groupCreated _ expects(id, *) returning 0.pure[ConnectionIO]
        context.roleClient.createRole _ expects savedLDAP.securityRole returning IO.unit
        context.ldapRepository.roleCreated _ expects(id, *) returning 0.pure[ConnectionIO]
        context.roleClient.grantGroup _ expects(savedLDAP.commonName, savedLDAP.securityRole) returning IO.unit
        context.ldapRepository.groupAssociated _ expects(id, *) returning 0.pure[ConnectionIO]
        context.roleClient.enableAccessToDB _ expects(savedHive.name, savedLDAP.securityRole, Manager) returning IO.unit
        context.databaseGrantRepository.databaseGranted _ expects(id, *) returning 0.pure[ConnectionIO]
        context.roleClient.enableAccessToLocation _ expects(savedHive.location, savedLDAP.securityRole) returning IO.unit
        context.databaseGrantRepository.locationGranted _ expects(id, *) returning 0.pure[ConnectionIO]

        context.provisioningLDAPClient.createGroup _ expects(savedLDAP.commonName, *) returning IO.unit
        context.ldapRepository.groupCreated _ expects(id, *) returning 0.pure[ConnectionIO]
        context.roleClient.createRole _ expects savedLDAP.securityRole returning IO.unit
        context.ldapRepository.roleCreated _ expects(id, *) returning 0.pure[ConnectionIO]
        context.roleClient.grantGroup _ expects(savedLDAP.commonName, savedLDAP.securityRole) returning IO.unit
        context.ldapRepository.groupAssociated _ expects(id, *) returning 0.pure[ConnectionIO]
        context.roleClient.enableAccessToDB _ expects(savedHive.name, savedLDAP.securityRole, ReadOnly) returning IO.unit
        context.databaseGrantRepository.databaseGranted _ expects(id, *) returning 0.pure[ConnectionIO]
        context.roleClient.enableAccessToLocation _ expects(savedHive.location, savedLDAP.securityRole) returning IO.unit
        context.databaseGrantRepository.locationGranted _ expects(id, *) returning 0.pure[ConnectionIO]
      }

      inSequence {
        context.provisioningLDAPClient.addUserToGroup _ expects(savedLDAP.distinguishedName, standardUserDN) returning OptionT.some(standardUserDN.value)
        context.memberRepository.complete _ expects(id, standardUserDN) returning 0.pure[ConnectionIO]
      }

      inSequence {
        context.databaseRepository.findByWorkspace _ expects id returning List(savedHive).pure[ConnectionIO]
        context.hiveClient.createTable _ expects (savedHive.name, ImpalaServiceImpl.TEMP_TABLE_NAME) returning 0.pure[IO]
        context.impalaClient.get.invalidateMetadata _ expects (savedHive.name, ImpalaServiceImpl.TEMP_TABLE_NAME) returning ().pure[IO]
        context.hiveClient.dropTable _ expects (savedHive.name, ImpalaServiceImpl.TEMP_TABLE_NAME) returning ().pure[IO]
        context.hiveClient.dropTable _ expects (savedHive.name, ImpalaServiceImpl.HEIMDALI_TEMP_TABLE_NAME) returning ().pure[IO]
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


  it should "deprovision a workspace" in new Context {
    inSequence {
      context.provisioningLDAPClient.removeUserFromGroup _ expects(savedLDAP.distinguishedName, standardUserDN) returning OptionT.some(standardUserDN.value)

      inSequence {
        context.roleClient.removeAccessToLocation _ expects(savedHive.location, savedLDAP.securityRole) returning IO.unit
        context.roleClient.removeAccessToDB _ expects(savedHive.name, savedLDAP.securityRole, ReadOnly) returning IO.unit
        context.roleClient.revokeGroup _ expects(savedLDAP.commonName, savedLDAP.securityRole) returning IO.unit

        context.roleClient.dropRole _ expects savedLDAP.securityRole returning IO.unit
        context.provisioningLDAPClient.deleteGroup _ expects savedLDAP.distinguishedName returning OptionT.some(standardUserDN.value)
        context.roleClient.removeAccessToLocation _ expects(savedHive.location, savedLDAP.securityRole) returning IO.unit
        context.roleClient.removeAccessToDB _ expects(savedHive.name, savedLDAP.securityRole, Manager) returning IO.unit
        context.roleClient.revokeGroup _ expects(savedLDAP.commonName, savedLDAP.securityRole) returning IO.unit

        context.roleClient.dropRole _ expects savedLDAP.securityRole returning IO.unit
        context.provisioningLDAPClient.deleteGroup _ expects savedLDAP.distinguishedName returning OptionT.some(standardUserDN.value)
        context.hiveClient.dropDatabase _ expects savedHive.name returning IO(1)
        context.hdfsService.removeQuota _ expects(savedHive.location) returning IO.unit
      }

    }

    provisioningService
      .attemptDeprovision(savedWorkspaceRequest)
      .unsafeRunSync()
      .join
      .unsafeRunSync()
      .map(x => println(x.message))
  }

  trait Context {
    implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

    val context: AppContext[IO] = genMockContext()

    val ldapUser = LDAPUser(personName, standardUsername, standardUserDN, Seq("cn=foo,dc=jotunn,dc=io"), Some("dude@email.com"))

    lazy val provisioningService = new DefaultProvisioningService[IO](context, ExecutionContext.global)
  }

}
