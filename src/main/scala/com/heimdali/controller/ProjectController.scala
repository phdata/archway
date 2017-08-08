package com.heimdali.controller

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.DeadboltActions
import com.heimdali.models.Project
import com.heimdali.services.ProjectService
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProjectController @Inject()(controllerComponents: MessagesControllerComponents,
                                  actionBuilder: DeadboltActions,
                                  projectService: ProjectService)
                                 (implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(controllerComponents) {

  implicit val projectReads: Reads[Project] = (
    Reads.pure(None) and
      (__ \ "name").read[String] and
      (__ \ "purpose").read[String] and
      Reads.pure(DateTime.now()) and
      Reads.pure("")
    ) (Project.apply _)

  import play.api.libs.json.JodaWrites._
  implicit val projectWrites: Writes[Project] = (
    (__ \ "id").writeNullable[Long] and
      (__ \ "name").write[String] and
      (__ \ "purpose").write[String] and
      (__ \ "created").write[DateTime] and
      (__ \ "created_by").write[String]
    ) (unlift(Project.unapply)
  )

  def requestNew: Action[JsValue] = actionBuilder.SubjectPresent()(parse.json) { request =>
    request.body.validate[Project].fold(
      errors => {
        Future(BadRequest(Json.obj("errors" -> JsError.toJson(errors))))
      },
      project => {
        val projectWithUsername = project.copy(createdBy = request.subject.get.identifier)
        projectService.create(projectWithUsername).map { newProject =>
          Created(Json.toJson(newProject))
        }
      }
    )
  }

}
