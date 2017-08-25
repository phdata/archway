package com.heimdali.controller

import javax.inject.Inject

import be.objectify.deadbolt.scala.DeadboltActions
import com.heimdali.services.{Cluster, ClusterService, Distribution}
import play.api.libs.json.Json
import play.api.mvc.{Action, MessagesAbstractController, MessagesControllerComponents}
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.ExecutionContext
import julienrf.json.derived
import julienrf.json.derived.NameAdapter

class ClusterController @Inject()(controllerComponents: MessagesControllerComponents,
                                  clusterService: ClusterService,
                                  actionBuilder: DeadboltActions)
                                 (implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(controllerComponents) {

  implicit val distributionWrite: OWrites[Distribution] = derived.flat.owrites((__ \ "name").write[String])
  implicit val clusterWrite: Writes[Cluster] = Json.writes[Cluster]

  def list = actionBuilder.SubjectPresent()() { implicit request =>
    clusterService.list.map { clusters =>
      Ok(Json.toJson(clusters))
    }
  }

}
