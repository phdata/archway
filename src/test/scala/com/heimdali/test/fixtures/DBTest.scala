package com.heimdali.test.fixtures

import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.{BeforeAndAfterEach, Suite}

trait DBTest extends BeforeAndAfterEach { this: Suite =>

  val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/heimdali",
    "postgres",
    ""
  )
}
