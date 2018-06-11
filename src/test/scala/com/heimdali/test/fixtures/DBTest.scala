package com.heimdali.test.fixtures

import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.{BeforeAndAfterEach, Suite}

trait DBTest extends BeforeAndAfterEach { this: Suite =>

  val transactor = Transactor.fromDriverManager[IO](
    "com.mysql.jdbc.Driver",
    "jdbc:mysql://localhost:3306/heimdali",
    "root",
    "my-secret-pw"
  )
}
