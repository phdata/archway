package com.heimdali.services

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer

import scala.concurrent.Future

trait HttpClient {
  def request(httpRequest: HttpRequest): Future[HttpResponse]
}

class AkkaHttpClient(httpExt: HttpExt)
                    (implicit materrializer: Materializer) extends HttpClient {
  override def request(httpRequest: HttpRequest): Future[HttpResponse] =
    httpExt.singleRequest(httpRequest)
}