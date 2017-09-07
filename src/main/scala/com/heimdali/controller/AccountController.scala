package com.heimdali.controller

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import com.heimdali.services.{AccountService, HeimdaliRole, User}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountController @Inject()(controllerComponents: MessagesControllerComponents,
                                  accountService: AccountService,
                                  actionBuilder: DeadboltActions)
                                 (implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(controllerComponents) {

  def token(): Action[AnyContent] = Action.async(parse.anyContent) { implicit request =>
    request.headers.get(AUTHORIZATION) match {
      case Some(Credentials(username, password)) => accountService.login(username, password).flatMap {
        case Some(user) => accountService.refresh(user).map(t => Ok(Json.toJson(t)))
        case None => Future { BadRequest(request.messages("api.error.badrequest")) }
      }
      case None => Future {
        BadRequest(request.messages("api.error.badrequest"))
      }
    }
  }

  def profile() = actionBuilder.SubjectPresent()() { implicit request =>
    implicit val reads: Writes[User] = (
      (__ \ "name").write[String] ~
        (__ \ "username").write[String] ~
        (__ \ "role").write[String].contramap[HeimdaliRole](_.name)
      ) (unlift(User.unapply))

    request.subject.get match {
      case profile: User => Future { Ok(Json.toJson(profile)) }
    }
  }

}