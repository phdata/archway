package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import com.heimdali.actors.LDAPActor.{CreateEntry, LDAPDone}
import com.heimdali.actors.ProjectProvisioner.{ProvisionCompleted, RegisterCaller, Request}
import com.heimdali.actors.ProjectSaver.{ProjectSaved, UpdateProject}
import com.heimdali.test.fixtures.TestProject
import org.scalatest.{FlatSpec, Matchers}

class ProjectProvisionerSpec extends FlatSpec with Matchers {

  behavior of "Project Provisioner"

  it should "ask to create" in {
    implicit val actorSystem = ActorSystem()

    val mainProbe = TestProbe("main")
    val ldapProbe = TestProbe("ldap")
    val saveProbe = TestProbe("save")

    val project = TestProject()
    val dn = s"cn=edh_sw_${project.generatedName},ou=groups,ou=hadoop,dc=jotunn,dc=io"

    val provisioner = actorSystem.actorOf(Props(classOf[ProjectProvisioner], ldapProbe.ref, saveProbe.ref))

    provisioner ! RegisterCaller(mainProbe.ref)
    provisioner ! Request(project)
    ldapProbe.expectMsg(CreateEntry(project.generatedName, Seq(project.createdBy)))
    ldapProbe.reply(LDAPDone(dn))
    saveProbe.expectMsg(UpdateProject(project.copy(ldapDn = Some(dn))))
    saveProbe.reply(ProjectSaved)
    mainProbe.expectMsg(ProvisionCompleted)
  }
}
