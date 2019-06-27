package com.heimdali.rest

import java.io.File

import cats.effect._
import com.heimdali.AppContext
import com.typesafe.scalalogging.LazyLogging
import org.http4s._
import org.http4s.dsl.Http4sDsl

import scala.concurrent.ExecutionContext

class StaticContentController[F[_] : ContextShift : Sync](appContext: AppContext[F], blockingEc: ExecutionContext)
  extends Http4sDsl[F] with LazyLogging{

  val route: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req @ GET -> Root / path =>
        val fullFilePath = s"$appContext.appConfig.ui.staticContentDir}/$path"
        logger.info(fullFilePath)
        StaticFile.fromFile(new File(fullFilePath), blockingEc, Some(req))
          .getOrElseF(NotFound())
    }
  }

}
