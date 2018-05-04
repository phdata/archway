package com.heimdali.provisioning

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{FlatSpec, Matchers}

class SharedWorkspaceProvisionerSpec extends FlatSpec with Matchers {

  behavior of "Project Provisioner"

  ignore should "ask to create" in new TestKit(ActorSystem()) with ImplicitSender {
//    val mainProbe = TestProbe("main")
//
//    val dn = s"cn=edh_sw_${project.systemName},ou=groups,ou=hadoop,dc=jotunn,dc=io"
//    val directory = s"/projects/${project.systemName}"
//    val keytab = new Path(s"$directory/${project.systemName}.keytab")
//    val poolName = "yarn test"
//
//    val provisioner = system.actorOf(WorkspaceProvisioner.props(testActor, testActor, testActor, testActor, testActor, testActor, project))
//
//    provisioner ! WorkspaceRequest()
//
//    expectMsg(Started)
//    expectMsg(CreateSharedWorkspaceGroup(project.systemName, Seq(project.createdBy)))
//    expectMsg(CreateSharedDirectory(project.systemName, 0.0))
//    provisioner ! LDAPRegistered(project.id, project.systemName)
//    provisioner ! HDFSUpdate(project.id, directory, 0.0)
  }
}

