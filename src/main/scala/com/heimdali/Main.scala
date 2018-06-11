package com.heimdali

import cats.effect.IO
import com.heimdali.modules._
import fs2.{Stream, StreamApp}

import scala.concurrent.ExecutionContext

object Main extends StreamApp[IO] {
  val heimdaliApp = new AppModule[IO]
    with ExecutionContextModule
    with ConfigurationModule
    with ContextModule[IO]
    with FileSystemModule[IO]
    with StartupModule[IO]
    with HttpModule[IO]
    with ClusterModule[IO]
    with ClientModule[IO]
    with RepoModule
    with ServiceModule[IO]
    with RestModule

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, StreamApp.ExitCode] = {
    implicit val ec: ExecutionContext =  heimdaliApp.executionContext

    heimdaliApp.startup.start().unsafeRunSync()
    heimdaliApp.restAPI.build().serve
  }
}
