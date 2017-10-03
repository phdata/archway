package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import com.heimdali.actors.ProjectSaver.{LDAPUpdate, ProjectSaved}
import com.heimdali.repositories.ProjectRepository
import com.heimdali.test.fixtures.TestProject
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class ProjectSaverTest extends FlatSpec with Matchers with MockFactory {

  behavior of "Project saver"

  it should "save a project" in {
    implicit val actorSystem = ActorSystem()
    implicit val executionContext = ExecutionContext.global

    val probe = TestProbe()
    val project = TestProject(ldapDn = Some("mydn"))
    val request = LDAPUpdate(project)

    val projectRepository = mock[ProjectRepository]
    (projectRepository.setLDAP _).expects(project.id, project.generatedName, project.ldapDn.get).returning(Future { project })

    val actor = actorSystem.actorOf(Props(classOf[ProjectSaver], projectRepository, executionContext))
    actor.tell(request, probe.ref)

    probe.expectMsg(ProjectSaved)
  }

}
