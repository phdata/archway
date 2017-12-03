package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestProbe}
import com.heimdali.actors.HDFSActor.{CreateDirectory, HDFSUpdate}
import com.heimdali.actors.KeytabActor.{GenerateKeytab, KeytabCreated}
import com.heimdali.actors.LDAPActor.{CreateEntry, LDAPUpdate}
import com.heimdali.actors.ProjectProvisioner.{ProvisionCompleted, RegisterCaller, Request}
import com.heimdali.actors.ProjectSaver.ProjectSaved
import com.heimdali.test.fixtures.TestProject
import org.apache.hadoop.fs.Path
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.Queue

class ProjectProvisionerSpec extends FlatSpec with Matchers {

  behavior of "Project Provisioner"

  it should "have correct default tasks" in {
    import HDFSActor._
    import KeytabActor._
    import LDAPActor._

    implicit val actorSystem = ActorSystem()

    val testProb = TestProbe()

    val project = TestProject()

    val provisioner = TestActorRef[ProjectProvisioner](Props(classOf[ProjectProvisioner], testProb.ref, testProb.ref, testProb.ref, testProb.ref, project)).underlyingActor

    provisioner.initialSteps should be(Queue(
      testProb.ref -> CreateEntry(project.id, project.systemName, Seq(project.createdBy)),
      testProb.ref -> CreateDirectory(project.id, project.systemName, project.hdfs.requestedSizeInGB, project.hdfs.actualGB),
      testProb.ref -> GenerateKeytab(project.id, project.systemName)
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
    val dn = s"cn=edh_sw_${project.generatedName},ou=groups,ou=hadoop,dc=jotunn,dc=io"
    val directory = s"/projects/${project.systemName}"
    val keytab = new Path(s"$directory/${project.systemName}.keytab")

    val provisioner = actorSystem.actorOf(Props(classOf[ProjectProvisioner], ldapProbe.ref, saveProbe.ref, hdfsProbe.ref, keytabProbe.ref, project))

    provisioner ! RegisterCaller(mainProbe.ref)
    provisioner ! Request

    ldapProbe.expectMsg(CreateEntry(project.id, project.generatedName, Seq(project.createdBy)))
    ldapProbe.reply(LDAPUpdate(project.id, project.systemName))
    saveProbe.expectMsg(LDAPUpdate(project.id, project.systemName))
    saveProbe.reply(ProjectSaved)

    hdfsProbe.expectMsg(CreateDirectory(project.id, project.systemName, project.hdfs.requestedSizeInGB, project.hdfs.actualGB))
    hdfsProbe.reply(HDFSUpdate(project.id, directory, 10.0))
    saveProbe.expectMsg(HDFSUpdate(project.id, directory, 10.0))
    saveProbe.reply(ProjectSaved)

    keytabProbe.expectMsg(GenerateKeytab(project.id, project.systemName))
    keytabProbe.reply(KeytabCreated(project.id, keytab))
    saveProbe.expectMsg(KeytabCreated(project.id, keytab))
    saveProbe.reply(ProjectSaved)

    mainProbe.expectMsg(ProvisionCompleted)
  }
}

