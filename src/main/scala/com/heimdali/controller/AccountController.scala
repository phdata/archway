package com.heimdali.controller

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.ActionBuilders
import com.heimdali.services.AccountService
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountController @Inject()(controllerComponents: MessagesControllerComponents,
                                  accountService: AccountService)
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

}