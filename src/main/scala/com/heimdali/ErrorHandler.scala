package com.heimdali

import javax.inject.{Inject, Singleton}

import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject() (implicit executionContext: ExecutionContext) extends HttpErrorHandler {

  def generateError(message: String, errorCode: Option[Int] = None): Future[JsObject] = Future {
    (message, errorCode) match {
      case ("", Some(code)) => Json.obj("error_code" -> code)
      case (msg, Some(code)) => Json.obj("error" -> msg, "error_code" -> code)
      case (msg, None) => Json.obj("error" -> msg)
      case _ => Json.obj("error" -> "Unknown error")
    }
  }

  def onClientError(request: RequestHeader, statusCode: Int, message: String) =
    generateError(message, Some(statusCode)).map(Status(statusCode)(_))

  def onServerError(request: RequestHeader, exception: Throwable) =
    generateError(exception.getMessage).map(InternalServerError(_))
}
