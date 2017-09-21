package com.heimdali.services

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import com.heimdali.actors.ProjectProvisioner.Request
import com.heimdali.repositories.ProjectRepository
import com.heimdali.test.fixtures._
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Future

class ProjectServiceImplSpec extends AsyncFlatSpec with Matchers with AsyncMockFactory {

  behavior of "ProjectServiceImpl"

  it should "create a project" in {
    implicit val actorSystem = ActorSystem()
    val probe = TestProbe()
    val repo = mock[ProjectRepository]
    val date = LocalDateTime.now
    val project = TestProject(id = 0L, createdDate = date)
    repo.create _ expects project returning Future { project.copy(id = 123L) }

    val projectServiceImpl = new ProjectServiceImpl(repo, probe.ref)
    projectServiceImpl.create(project) map { newProject =>
      probe.expectMsg(Request(newProject))

      newProject should have (
        'id (123L),
        'name (project.name),
        'purpose (project.purpose),
        'created (date),
        'createdBy (project.createdBy)
      )
    }
  }

  it should "list projects" in {
    implicit val actorSystem = ActorSystem()
    val probe = TestProbe()
    val repo = mock[ProjectRepository]
    val Array(project1, project2) = Array(
      TestProject(id = 123L),
      TestProject(id = 321L)
    )
    repo.list _ expects standardUsername returning Future { Seq(project1) }

    val projectServiceImpl = new ProjectServiceImpl(repo, probe.ref)
    projectServiceImpl.list(standardUsername) map { projects =>
      projects.length should be (1)
      projects.head should be (project1)
    }
  }

}
