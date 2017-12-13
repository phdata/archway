package com.heimdali.services

import java.time.LocalDateTime
import java.util.concurrent.LinkedBlockingDeque

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.TestActor.Message
import akka.testkit.{TestActor, TestActorRef, TestKit, TestKitBase, TestProbe}
import com.heimdali.actors.WorkspaceProvisioner
import com.heimdali.actors.WorkspaceProvisioner.Request
import com.heimdali.models.Project
import com.heimdali.repositories.WorkspaceRepository
import com.heimdali.test.fixtures._
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Future

class SharedWorkspaceServiceImplSpec extends AsyncFlatSpec with Matchers with AsyncMockFactory {

  behavior of "ProjectServiceImpl"

  it should "create a project" in {
    val probe = TestProbe()
    val repo = mock[WorkspaceRepository]
    val date = LocalDateTime.now
    val project = TestProject(id = 0L, createdDate = date)
    repo.create _ expects project returning Future {
      project.copy(id = 123L)
    }

    val projectServiceImpl = new WorkspaceServiceImpl(repo, factory)
    projectServiceImpl.create(project) map { newProject =>
      newProject should have(
        'id (123L),
        'name (project.name),
        'purpose (project.purpose),
        'compliance (project.compliance),
        'created (date),
        'createdBy (project.createdBy)
      )
    }
  }

  it should "list projects" in {
    val repo = mock[WorkspaceRepository]
    val Array(project1, project2) = Array(
      TestProject(id = 123L),
      TestProject(id = 321L)
    )
    repo.list _ expects standardUsername returning Future {
      Seq(project1)
    }

    val projectServiceImpl = new WorkspaceServiceImpl(repo, factory)
    projectServiceImpl.list(standardUsername) map { projects =>
      projects.length should be(1)
      projects.head should be(project1)
    }
  }

  implicit val system: ActorSystem = ActorSystem()

  def factory(project: Project): ActorRef =
    TestActorRef.create(system, Props(classOf[TestActor], new LinkedBlockingDeque[Message]()))

}
