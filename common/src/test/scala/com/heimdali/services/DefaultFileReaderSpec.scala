package com.heimdali.services

import cats.effect.IO
import org.scalatest.{FlatSpec, Matchers}

class DefaultFileReaderSpec extends FlatSpec with Matchers {

  behavior of "DefaultFileReaderSpec"

  it should "readLines" in {
    val expected = List("-myflag=123", "-flagmy=321")

    val reader = new DefaultFileReader[IO]()

    val actual = reader.readLines("flags.flags").unsafeRunSync()

    actual shouldBe expected
  }

}
