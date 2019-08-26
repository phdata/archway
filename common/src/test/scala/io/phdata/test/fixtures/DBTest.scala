package io.phdata.test.fixtures

import cats.effect.IO
import doobie.util.transactor.Transactor
import io.phdata.config.Password
import org.scalatest.{BeforeAndAfterEach, Suite}

import scala.concurrent.ExecutionContext

trait DBTest extends BeforeAndAfterEach { this: Suite =>

  implicit val contextShift = IO.contextShift(ExecutionContext.global)

  val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
    appConfig.db.meta.driver,
    appConfig.db.meta.url,
    appConfig.db.meta.username.getOrElse(""),
    appConfig.db.meta.password.getOrElse(Password("")).value
  )
}
