package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestProbe}
import com.heimdali.actors.HDFSActor.{CreateDirectory, HDFSDone}
import com.heimdali.actors.LDAPActor.{CreateEntry, LDAPDone}
import com.heimdali.actors.ProjectProvisioner.{ProvisionCompleted, RegisterCaller, Request}
import com.heimdali.actors.ProjectSaver.{HDFSUpdate, LDAPUpdate, ProjectSaved}
import com.heimdali.test.fixtures.TestProject
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ListBuffer

class ProjectProvisionerSpec extends FlatSpec with Matchers {

  behavior of "Project Provisioner"

  it should "have correct default tasks" in {
    implicit val actorSystem = ActorSystem()

    val testProb = TestProbe()

    val provisioner = TestActorRef[ProjectProvisioner](Props(classOf[ProjectProvisioner], testProb.ref, testProb.ref, testProb.ref)).underlyingActor

    provisioner.initialSteps should be (ListBuffer(
      ProjectProvisioner.CreateLDAPEntry,
      ProjectProvisioner.CreateHDFSAllocations
    ))
  }

  it should "ask to create" in {
    implicit val actorSystem = ActorSystem()

    val mainProbe = TestProbe("main")
    val ldapProbe = TestProbe("ldap")
    val saveProbe = TestProbe("save")
    val hdfsProbe = TestProbe("hdfs")

    val project = TestProject()
    val dn = s"cn=edh_sw_${project.generatedName},ou=groups,ou=hadoop,dc=jotunn,dc=io"
    val ldapProject = project.copy(ldapDn = Some(dn))
    val directory = s"/projects/${project.systemName}"
    val hdfsProject = ldapProject.copy(hdfs = project.hdfs.copy(location = Some(directory)))

    val provisioner = actorSystem.actorOf(Props(classOf[ProjectProvisioner], ldapProbe.ref, saveProbe.ref, hdfsProbe.ref))

    provisioner ! RegisterCaller(mainProbe.ref)
    provisioner ! Request(project)
    ldapProbe.expectMsg(CreateEntry(project.generatedName, Seq(project.createdBy)))
    ldapProbe.reply(LDAPDone(dn))
    saveProbe.expectMsg(LDAPUpdate(ldapProject))
    saveProbe.reply(ProjectSaved)
    hdfsProbe.expectMsg(CreateDirectory(project.systemName, project.hdfs.requestedSizeInGB))
    hdfsProbe.reply(HDFSDone(directory))
    saveProbe.expectMsg(HDFSUpdate(hdfsProject))
    saveProbe.reply(ProjectSaved)
    mainProbe.expectMsg(ProvisionCompleted)
  }
}

