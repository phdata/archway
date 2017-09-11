package com.heimdali

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}
import com.heimdali.services.{AccountService, User}
import play.api.libs.json.Json
import play.api.mvc.{Request, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

class HeimdaliHandler(accountService: AccountService)
                     (implicit executionContext: ExecutionContext) extends DeadboltHandler {

  override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] =
    Future(None)

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] =
    Future(None)

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[User]] = {
    val tokenExtractor = """Bearer (.*)""".r
    request.headers.get("Authorization").map {
      case tokenExtractor(token) => accountService.validate(token)
      case _ => Future(None)
    }.getOrElse(Future(None))
  }

  override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] =
    Future {
      Results.Unauthorized(Json.obj("error" -> "Unauthorized"))
    }

}
