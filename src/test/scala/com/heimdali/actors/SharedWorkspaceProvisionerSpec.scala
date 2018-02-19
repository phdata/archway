package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestProbe}
import com.heimdali.actors.HDFSActor.{CreateSharedDirectory, HDFSUpdate}
import com.heimdali.actors.KeytabActor.{GenerateKeytab, KeytabCreated}
import com.heimdali.actors.LDAPActor.{CreateSharedWorkspaceGroup, LDAPUpdate}
import com.heimdali.actors.workspace.WorkspaceProvisioner.{ProvisionCompleted, RegisterCaller, Request}
import com.heimdali.actors.WorkspaceSaver.WorkspaceSaved
import com.heimdali.actors.YarnActor.{CreatePool, PoolCreated}
import com.heimdali.actors.workspace.WorkspaceProvisioner
import com.heimdali.test.fixtures.TestProject
import org.apache.hadoop.fs.Path
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.Queue

class SharedWorkspaceProvisionerSpec extends FlatSpec with Matchers {

  behavior of "Project Provisioner"

  it should "have correct default tasks" in {
    import HDFSActor._
    import KeytabActor._
    import LDAPActor._

    implicit val actorSystem = ActorSystem()

    val testProb = TestProbe()

    val project = TestProject()

    val provisioner = TestActorRef[WorkspaceProvisioner](Props(classOf[WorkspaceProvisioner], testProb.ref, testProb.ref, testProb.ref, testProb.ref, testProb.ref, project)).underlyingActor

    provisioner.initialSteps should be(Queue(
      testProb.ref -> CreateSharedWorkspaceGroup(project.id, project.systemName, Seq(project.createdBy)),
      testProb.ref -> CreateDirectory(project.id, project.systemName, project.hdfs.requestedSizeInGB)
    ))
  }

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

    provisioner ! RegisterCaller(mainProbe.ref)
    provisioner ! Request

    ldapProbe.expectMsg(CreateSharedWorkspaceGroup(project.id, project.systemName, Seq(project.createdBy)))
    ldapProbe.reply(LDAPUpdate(project.id, project.systemName))
    saveProbe.expectMsg(LDAPUpdate(project.id, project.systemName))
    saveProbe.reply(WorkspaceSaved)

    hdfsProbe.expectMsg(CreateDirectory(project.id, project.systemName, project.hdfs.requestedSizeInGB))
    hdfsProbe.reply(HDFSUpdate(project.id, directory, 10.0))
    saveProbe.expectMsg(HDFSUpdate(project.id, directory, 10.0))
    saveProbe.reply(WorkspaceSaved)

    mainProbe.expectMsg(ProvisionCompleted)
  }
}

