package com.heimdali

import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp}
import com.heimdali.config.AppConfig
import com.heimdali.modules._
import com.heimdali.templates.WorkspaceGenerator
import pureconfig.{CamelCase, ConfigFieldMapping, ProductHint}

import scala.concurrent.ExecutionContext

trait Server extends IOApp {

  val heimdaliApp = new IOAppModule[IO]
    with ExecutionContextModule[IO]
    with ConfigurationModule
    with FileSystemModule[IO]
    with StartupModule[IO]
    with HttpModule[IO]
    with ClientModule[IO]
    with RepoModule
    with ServiceModule[IO]
    with RestModule

  override def run(args: List[String]): IO[ExitCode] = {
    val startupContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
    val startupShift = IO.contextShift(startupContext)

    for {
      _ <- heimdaliApp.startup.start().start(startupShift)
      result <- heimdaliApp.restAPI.build()
    } yield result
  }
}
