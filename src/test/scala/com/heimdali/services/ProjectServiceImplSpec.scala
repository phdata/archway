package com.heimdali.services

import java.time.LocalDateTime

import com.heimdali.models.Project
import com.heimdali.repositories.ProjectRepository
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Future

class ProjectServiceImplSpec extends AsyncFlatSpec with Matchers with AsyncMockFactory {

  behavior of "ProjectServiceImpl"

  it should "create a project" in {
    val repo = mock[ProjectRepository]
    val date = LocalDateTime.now
    val project = Project(0L, "sesame", "something", date, "username")
    repo.create _ expects project returning Future { project.copy(id = 123L) }

    val projectServiceImpl = new ProjectServiceImpl(repo)
    projectServiceImpl.create(project) map { newProject =>
      newProject should have (
        'id (123),
        'name (project.name),
        'purpose (project.purpose),
        'created (date),
        'createdBy (project.createdBy)
      )
    }
  }

  it should "list projects" in {
    val repo = mock[ProjectRepository]
    val username = "username"
    val Array(project1, project2) = Array(
      Project(123L, "Project 1", "Stuff", LocalDateTime.now(), "username"),
      Project(321L, "Project 2", "Stuff", LocalDateTime.now(), "someone")
    )
    repo.list _ expects username returning Future { Seq(project1) }

    val projectServiceImpl = new ProjectServiceImpl(repo)
    projectServiceImpl.list(username) map { projects =>
      projects.length should be (1)
      projects.head should be (project1)
    }
  }

}
