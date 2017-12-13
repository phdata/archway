package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestProbe}
import com.heimdali.actors.HDFSActor.{CreateDirectory, HDFSUpdate}
import com.heimdali.actors.KeytabActor.{GenerateKeytab, KeytabCreated}
import com.heimdali.actors.LDAPActor.{CreateEntry, LDAPUpdate}
import com.heimdali.actors.WorkspaceProvisioner.{ProvisionCompleted, RegisterCaller, Request}
import com.heimdali.actors.WorkspaceSaver.WorkspaceSaved
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

    val provisioner = TestActorRef[WorkspaceProvisioner](Props(classOf[WorkspaceProvisioner], testProb.ref, testProb.ref, testProb.ref, testProb.ref, project)).underlyingActor

    provisioner.initialSteps should be(Queue(
      testProb.ref -> CreateEntry(project.id.get, project.systemName.get, Seq(project.createdBy)),
      testProb.ref -> CreateDirectory(project.id.get, project.systemName.get, project.hdfs.requestedSizeInGB),
      testProb.ref -> GenerateKeytab(project.id.get, project.systemName.get)
    ))
  }

  it should "ask to create" in {
    implicit val actorSystem = ActorSystem()

    val mainProbe = TestProbe("main")
    val ldapProbe = TestProbe("ldap")
    val saveProbe = TestProbe("save")
    val hdfsProbe = TestProbe("hdfs")
    val keytabProbe = TestProbe("keytab")

    val project = TestProject()
    val dn = s"cn=edh_sw_${project.systemName},ou=groups,ou=hadoop,dc=jotunn,dc=io"
    val directory = s"/projects/${project.systemName}"
    val keytab = new Path(s"$directory/${project.systemName}.keytab")

    val provisioner = actorSystem.actorOf(Props(classOf[WorkspaceProvisioner], ldapProbe.ref, saveProbe.ref, hdfsProbe.ref, keytabProbe.ref, project))

    provisioner ! RegisterCaller(mainProbe.ref)
    provisioner ! Request

    ldapProbe.expectMsg(CreateEntry(project.id.get, project.systemName.get, Seq(project.createdBy)))
    ldapProbe.reply(LDAPUpdate(project.id.get, project.systemName.get))
    saveProbe.expectMsg(LDAPUpdate(project.id.get, project.systemName.get))
    saveProbe.reply(WorkspaceSaved)

    hdfsProbe.expectMsg(CreateDirectory(project.id.get, project.systemName.get, project.hdfs.requestedSizeInGB))
    hdfsProbe.reply(HDFSUpdate(project.id.get, directory))
    saveProbe.expectMsg(HDFSUpdate(project.id.get, directory))
    saveProbe.reply(WorkspaceSaved)

    keytabProbe.expectMsg(GenerateKeytab(project.id.get, project.systemName.get))
    keytabProbe.reply(KeytabCreated(project.id.get, keytab))
    saveProbe.expectMsg(KeytabCreated(project.id.get, keytab))
    saveProbe.reply(WorkspaceSaved)

    mainProbe.expectMsg(ProvisionCompleted)
  }
}

