package com.heimdali.provisioning

import akka.actor
import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestKitBase}
import com.heimdali.provisioning.WorkspaceSaver.{HiveUpdate, LDAPUpdate, YarnUpdate}
import com.heimdali.repositories.{HiveDatabaseRepository, LDAPRepository, SharedWorkspaceRepository, YarnRepository}
import com.heimdali.test.fixtures._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SharedWorkspaceSaverTest extends FlatSpec with Matchers with MockFactory {

  behavior of "Project saver"

  it should "update LDAP a project" in new TestKit(ActorSystem()) with ImplicitSender with Context {
    val newLdap = ldap.copy(id = Some(entityId))
    ldapRepository.create _ expects ldap returning Future(newLdap)
    workspaceRepository.setLDAP _ expects(workspaceId, entityId) returning Future(initialSharedWorkspace)

    val actor = actorFactory
    actor ! LDAPUpdate(workspaceId, ldap)

    expectMsg(initialSharedWorkspace)
  }

  it should "update Hive a project" in new TestKit(ActorSystem()) with ImplicitSender with Context {
    val newHive = hive.copy(id = Some(entityId))
    hiveRepository.create _ expects hive returning Future(newHive)
    workspaceRepository.setHive _ expects(workspaceId, entityId) returning Future(initialSharedWorkspace)

    val actor = actorFactory
    actor ! HiveUpdate(workspaceId, hive)

    expectMsg(initialSharedWorkspace)
  }

  it should "update YARN a project" in new TestKit(ActorSystem()) with ImplicitSender with Context {
    val newYarn = yarn.copy(id = Some(entityId))
    yarnRepository.create _ expects yarn returning Future(newYarn)
    workspaceRepository.setYarn _ expects(workspaceId, entityId) returning Future(initialSharedWorkspace)

    val actor = actorFactory
    actor ! YarnUpdate(workspaceId, yarn)

    expectMsg(initialSharedWorkspace)
  }

  trait Context { this: TestKitBase =>
    val (workspaceId, entityId) = (123L, 123L)

    val ldapRepository: LDAPRepository = mock[LDAPRepository]
    val hiveRepository: HiveDatabaseRepository = mock[HiveDatabaseRepository]
    val yarnRepository: YarnRepository = mock[YarnRepository]
    val workspaceRepository: SharedWorkspaceRepository = mock[SharedWorkspaceRepository]


    def actorFactory: ActorRef =
      system.actorOf(WorkspaceSaver(workspaceRepository, ldapRepository, hiveRepository, yarnRepository))

  }

}
