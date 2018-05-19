package com.heimdali.provisioning

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.heimdali.clients.HDFSAllocation
import com.heimdali.models.{HiveDatabase, LDAPRegistration, SharedWorkspace}
import com.heimdali.provisioning.HDFSActor.{CreateDirectory, DirectoryCreated}
import com.heimdali.provisioning.HiveActor.{CreateDatabase, DatabaseCreated}
import com.heimdali.provisioning.LDAPActor.{CreateGroup, LDAPGroupCreated}
import com.heimdali.provisioning.WorkspaceProvisioner.{Start, Started}
import com.heimdali.provisioning.WorkspaceSaver.{HiveUpdate, LDAPUpdate, YarnUpdate}
import com.heimdali.provisioning.YarnActor.{CreatePool, PoolCreated}
import com.typesafe.config.ConfigFactory
import com.heimdali.test.fixtures._
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.Queue

class WorkspaceProvisionerSpec extends FlatSpec with Matchers {

  behavior of "Project Provisioner"

  it should "ask to create" in new TestKit(ActorSystem()) with ImplicitSender {
    val mainProbe = TestProbe("main")
    val groupName = s"edh_sw_$systemName"
    val dirName = s"/data/shared_workspaces/$systemName"
    val databaseName = s"sw_$systemName"
    val roleName = s"role_sw_$systemName"

    val provisioner = system.actorOf(WorkspaceProvisioner.props[Long, SharedWorkspace](testActor, testActor, testActor, testActor, testActor, ConfigFactory.defaultApplication(), initialSharedWorkspace))

    provisioner ! Start

    expectMsg(CreateGroup(groupName, Seq(standardUsername)))
    expectMsg(CreateDirectory(dirName, hdfsRequestedSize, None))
    expectMsg(CreatePool(Queue("root"), s"sw_$systemName", maxCores, maxMemoryInGB))

    expectMsg(Started)

    provisioner ! DirectoryCreated(HDFSAllocation(dirName, hdfsRequestedSize))
    expectMsg(CreateDatabase(groupName, databaseName, roleName, dirName))

    provisioner ! DatabaseCreated(hive)
    expectMsg(HiveUpdate(123, hive))

    provisioner ! LDAPGroupCreated(groupName, ldapDn)
    expectMsg(LDAPUpdate(123, ldap))

    provisioner ! PoolCreated(poolName)
    expectMsg(YarnUpdate(123, yarn))
  }
}

