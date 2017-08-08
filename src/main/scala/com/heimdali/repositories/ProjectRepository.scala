package com.heimdali.repositories

import javax.inject.Inject

import com.heimdali.models.Project
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import com.github.tototoshi.slick.MySQLJodaSupport._
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait ProjectRepository {

  def create(project: Project): Future[Project]

}

class ProjectRepositoryImpl @Inject() (val dbConfigProvider: DatabaseConfigProvider)
                                      (implicit ec: ExecutionContext)
  extends ProjectRepository
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private class ProjectTable(tag: Tag) extends Table[Project](tag, "projects") {

    val id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    val name = column[String]("name")
    val purpose = column[String]("purpose")
    val created = column[DateTime]("created")
    val createdBy = column[String]("created_by")

    def * = (id.?, name, purpose, created, createdBy) <> (Project.tupled, Project.unapply)
  }

  private val query = TableQuery[ProjectTable]
  private val projectInc = query returning query.map(_.id) into ((project, id) => project.copy(id = Some(id)))

  def create(project: Project): Future[Project] =
    db.run(projectInc += project)

  def createTable() = db.run {DBIOAction.seq(
    query.schema.create
  )}

}