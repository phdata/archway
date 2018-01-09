package com.heimdali

import com.heimdali.modules._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {

  val app = new AppModule
    with ExecutionContextModule
    with ConfigurationModule
    with ContextModule
    with FileSystemModule
    with StartupModule
    with ClientModule
    with RepoModule
    with AkkaModule
    with HttpModule
    with ServiceModule
    with RestModule

  import app.executionContext

  Await.ready(
    for {
      _ <- app.startup.start()
      _ <- app.restAPI.start()
    } yield Unit, Duration.Inf)

}
