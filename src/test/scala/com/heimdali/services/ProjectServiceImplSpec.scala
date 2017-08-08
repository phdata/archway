package com.heimdali.services

import com.heimdali.models.Project
import com.heimdali.repositories.ProjectRepository
import org.joda.time.DateTime
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Future

class ProjectServiceImplSpec extends AsyncFlatSpec with Matchers with AsyncMockFactory {

  behavior of "ProjectServiceImpl"

  it should "create a project" in {
    val repo = mock[ProjectRepository]
    val date = DateTime.now
    val project = Project(None, "sesame", "something", date, "username")
    repo.create _ expects project returning Future { project.copy(id = Some(123)) }

    val projectServiceImpl = new ProjectServiceImpl(repo)
    projectServiceImpl.create(project) map { newProject =>
      newProject should have (
        'id (Some(123)),
        'name (project.name),
        'purpose (project.purpose),
        'created (date),
        'createdBy (project.createdBy)
      )
    }
  }

}
