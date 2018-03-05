package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import com.heimdali.actors.HDFSActor.CreateSharedDirectory
import com.heimdali.actors.LDAPActor.CreateSharedWorkspaceGroup
import com.heimdali.actors.WorkspaceSaver.{HDFSUpdate, LDAPUpdate, WorkspaceSaved}
import com.heimdali.actors.workspace.WorkspaceProvisioner
import com.heimdali.actors.workspace.WorkspaceProvisioner.Request
import com.heimdali.test.fixtures.TestProject
import org.apache.hadoop.fs.Path
import org.scalatest.{FlatSpec, Matchers}

class SharedWorkspaceProvisionerSpec extends FlatSpec with Matchers {

  behavior of "Project Provisioner"

  it should "ask to create" in {
    implicit val actorSystem = ActorSystem()

    val mainProbe = TestProbe("main")
    val ldapProbe = TestProbe("ldap")
    val saveProbe = TestProbe("save")
    val hdfsProbe = TestProbe("hdfs")
    val yarnProbe = TestProbe("yarn")
    val keytabProbe = TestProbe("keytab")

    val project = TestProject()
    val dn = s"cn=edh_sw_${project.systemName},ou=groups,ou=hadoop,dc=jotunn,dc=io"
    val directory = s"/projects/${project.systemName}"
    val keytab = new Path(s"$directory/${project.systemName}.keytab")
    val poolName = "yarn test"

    val provisioner = actorSystem.actorOf(Props(classOf[WorkspaceProvisioner], ldapProbe.ref, saveProbe.ref, hdfsProbe.ref, keytabProbe.ref, yarnProbe.ref, project))

    provisioner ! Request

    ldapProbe.expectMsg(CreateSharedWorkspaceGroup(project.systemName, Seq(project.createdBy)))
    ldapProbe.reply(LDAPUpdate(project.id, project.systemName))
    saveProbe.expectMsg(LDAPUpdate(project.id, project.systemName))
    saveProbe.reply(WorkspaceSaved)

    hdfsProbe.expectMsg(CreateSharedDirectory(project.systemName, project.hdfs.requestedSizeInGB))
    hdfsProbe.reply(HDFSUpdate(project.id, directory, 10.0))
    saveProbe.expectMsg(HDFSUpdate(project.id, directory, 10.0))
    saveProbe.reply(WorkspaceSaved)
  }
}

