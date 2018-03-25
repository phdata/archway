package com.heimdali.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.heimdali.actors.HDFSActor.CreateSharedDirectory
import com.heimdali.actors.LDAPActor.CreateSharedWorkspaceGroup
import com.heimdali.actors.WorkspaceSaver.{HDFSUpdate, LDAPUpdate}
import com.heimdali.actors.workspace.WorkspaceProvisioner
import com.heimdali.actors.workspace.WorkspaceProvisioner.{Request, Started}
import com.heimdali.test.fixtures.TestProject
import org.apache.hadoop.fs.Path
import org.scalatest.{FlatSpec, Matchers}

class SharedWorkspaceProvisionerSpec extends FlatSpec with Matchers {

  behavior of "Project Provisioner"

  it should "ask to create" in new TestKit(ActorSystem()) with ImplicitSender {
    val mainProbe = TestProbe("main")

    val project = TestProject()
    val dn = s"cn=edh_sw_${project.systemName},ou=groups,ou=hadoop,dc=jotunn,dc=io"
    val directory = s"/projects/${project.systemName}"
    val keytab = new Path(s"$directory/${project.systemName}.keytab")
    val poolName = "yarn test"

    val provisioner = system.actorOf(WorkspaceProvisioner.props(testActor, testActor, testActor, testActor, testActor, testActor, project))

    provisioner ! Request

    expectMsg(Started)
    expectMsg(CreateSharedWorkspaceGroup(project.systemName, Seq(project.createdBy)))
    expectMsg(CreateSharedDirectory(project.systemName, 0.0))
    provisioner ! LDAPUpdate(project.id, project.systemName)
    provisioner ! HDFSUpdate(project.id, directory, 0.0)
  }
}

