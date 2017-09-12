package com.heimdali.controller

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltActions}
import com.heimdali.models.Project
import com.heimdali.services.ProjectService
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

  implicit def projectReads(implicit request: AuthenticatedRequest[_]): Reads[Project] = (
    Reads.pure(0L) and
      (__ \ "name").read[String] and
      (__ \ "purpose").read[String] and
      Reads.pure(LocalDateTime.now()) and
      Reads.pure(request.subject.get.identifier)
    ) (Project.apply _)

  implicit val projectWrites: Writes[Project] = (
    (__ \ "id").write[Long] and
      (__ \ "name").write[String] and
      (__ \ "purpose").write[String] and
      (__ \ "created").write[LocalDateTime] and
      (__ \ "created_by").write[String]
    ) (unlift(Project.unapply)
  )

  def requestNew: Action[JsValue] = actionBuilder.SubjectPresent()(parse.json) { implicit request =>
    request.body.validate[Project].fold(
      errors => {
        Future(BadRequest(Json.obj("errors" -> JsError.toJson(errors))))
      },
      project => {
        projectService.create(project).map { newProject =>
          Created(Json.toJson(newProject))
        }
      }
    )
  }

  def list: Action[AnyContent] = actionBuilder.SubjectPresent()() { implicit request =>
    projectService.list(request.subject.get.identifier) map { projects =>
      Ok(Json.toJson(projects))
    }
  }

}
