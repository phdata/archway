package io.phdata.scheduledJobs

import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO}
import doobie.implicits._
import io.phdata.AppContext
import io.phdata.itest.fixtures.{KerberosTest, config, itestConfig, systemTestConfig}
import io.phdata.models.{DistinguishedName, LDAPRegistration}
import io.phdata.repositories.Group
import io.phdata.startup.ADGroupsSynchronizer
import io.phdata.test.fixtures.{TestTimer, _}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

import scala.concurrent.ExecutionContext

class ADGroupsSynchronizerIntegrationSpec extends FlatSpec with KerberosTest with BeforeAndAfterAll{

  val groupName = "test_group_name_pxr"
  val groupDN = DistinguishedName(s"cn=$groupName,${itestConfig.ldap.groupPath}")
  val userDN = DistinguishedName(s"CN=${systemTestConfig.existingUser},${itestConfig.ldap.userPath.get}")
  val ldapRegistration = LDAPRegistration(groupDN, "commonName", "sentryRole")

  implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  override def beforeAll = {
    new Context {
      resources.use {
        case (context, _) =>
          for {
            _ <- context.provisioningLDAPClient.createGroup(groupName, defaultLDAPAttributes(groupDN, groupName))
          } yield ()
      }.unsafeRunSync()
    }
  }

  override def afterAll() = {
    new Context {
      resources.use {
        case (context, _) =>
          for {
            _ <- context.provisioningLDAPClient.deleteGroup(groupDN).value
          } yield ()
      }.unsafeRunSync()
    }
  }

    it should "remove member from database" in new Context {
      val res: Option[Group] = resources
        .use {
          case (context, synchronizer) =>
            for {
              ldapReg <- context.ldapRepository.create(ldapRegistration).transact(context.transactor)
              _ <- context.memberRepository.create(userDN, ldapReg.id.get).transact(context.transactor)
              _ <- context.memberRepository.complete(ldapReg.id.get, userDN).transact(context.transactor)
              _ <- synchronizer.synchronize()
              groupMembers <- context.memberRepository.groupMembers.transact(context.transactor)

              // cleaning DB
              _ <- context.ldapRepository.delete(ldapRegistration).transact(context.transactor)
            } yield {
              groupMembers.find { case Group(group, userDNs) =>
                  groupDN == group && userDNs.contains(userDN)
                }
            }
        }.unsafeRunSync()

        assert(
          res.isEmpty
        )
    }

    it should "add member to the database" in new Context {

      val res: Option[Group] = resources
        .use {
          case (context, synchronizer) =>
            for {
              ldapReg <- context.ldapRepository.create(ldapRegistration).transact(context.transactor)
              _ <- context.provisioningLDAPClient.addUserToGroup(groupDN, userDN).value
              _ <- synchronizer.synchronize()
              groupMembers <- context.memberRepository.groupMembers.transact(context.transactor)

            // cleaning DB
            _ <- context.memberRepository.delete(ldapReg.id.get, userDN).transact(context.transactor)
            _ <- context.ldapRepository.delete(ldapRegistration).transact(context.transactor)
            } yield {
              groupMembers.find { case Group(group, userDNs) =>
                groupDN == group && userDNs.contains(userDN)
              }
            }
        }.unsafeRunSync()

      assert(
          res.isDefined
      )
    }


  trait Context {
    implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    implicit def timer = new TestTimer

    val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))

    val resources = for {
      context <- AppContext.default[IO](config.resolve())
      synchronizer = new ADGroupsSynchronizer(context)

    } yield (context, synchronizer)
  }

}

