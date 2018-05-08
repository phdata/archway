package com.heimdali.provisioning

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import com.heimdali.repositories.SharedWorkspaceRepository
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}
import com.heimdali.test.fixtures._

class SharedWorkspaceSaverTest extends FlatSpec with Matchers with MockFactory {

  behavior of "Project saver"

  ignore should "update HDFS a project" in {
//    implicit val actorSystem = ActorSystem()
//    implicit val executionContext = ExecutionContext.global
//
//    val (ldapId, datasetId) = (123, 123)
//
//    val probe = TestProbe()
//
//    val projectRepository = mock[SharedWorkspaceRepository]
//    (projectRepository.setLDAP _).expects(datasetId.toString, ldapId).returning(Future { initialSharedWorkspace })
//
//    val actor = actorSystem.actorOf(Props(classOf[WorkspaceSaver], projectRepository, executionContext))
//    actor.tell(request, probe.ref)
//
//    probe.expectMsg(WorkspaceSaved)
  }

}
