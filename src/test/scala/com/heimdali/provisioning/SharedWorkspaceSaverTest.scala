package com.heimdali.provisioning

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class SharedWorkspaceSaverTest extends FlatSpec with Matchers with MockFactory {

  behavior of "Project saver"

  //TODO: Switch to workspace saving

  ignore should "save a project" in {
//    implicit val actorSystem = ActorSystem()
//    implicit val executionContext = ExecutionContext.global
//
//    val probe = TestProbe()
//    val project = SharedWorkspaceRecord(TestProject(ldapDn = Some("mydn")))
//    val request = LDAPRegistered(project.id, project.ldapDn.get)
//
//    val projectRepository = mock[SharedWorkspaceRepository]
//    (projectRepository.setLDAP _).expects(project.id, project.ldapDn.get).returning(Future { project })
//
//    val actor = actorSystem.actorOf(Props(classOf[WorkspaceSaver], projectRepository, executionContext))
//    actor.tell(request, probe.ref)
//
//    probe.expectMsg(WorkspaceSaved)
  }

}
