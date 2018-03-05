package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import com.heimdali.actors.WorkspaceSaver.{LDAPUpdate, WorkspaceSaved}
import com.heimdali.repositories.WorkspaceRepository
import com.heimdali.test.fixtures.TestProject
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class SharedWorkspaceSaverTest extends FlatSpec with Matchers with MockFactory {

  behavior of "Project saver"

  it should "save a project" in {
    implicit val actorSystem = ActorSystem()
    implicit val executionContext = ExecutionContext.global

    val probe = TestProbe()
    val project = TestProject(ldapDn = Some("mydn"))
    val request = LDAPUpdate(project.id, project.ldapDn.get)

    val projectRepository = mock[WorkspaceRepository]
    (projectRepository.setLDAP _).expects(project.id, project.ldapDn.get).returning(Future { project })

    val actor = actorSystem.actorOf(Props(classOf[WorkspaceSaver], projectRepository, executionContext))
    actor.tell(request, probe.ref)

    probe.expectMsg(WorkspaceSaved)
  }

}
