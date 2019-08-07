package io.phdata.clients

import cats.effect.IO
import org.scalatest.FlatSpec
import io.phdata.itest.fixtures._

import scala.concurrent.ExecutionContext

class EmailClientImplIntegrationSpec extends FlatSpec {
  it should "Send an email" in new Context {
    System.setProperty("mail.debug", "true")

    val result = mailClient
      .send("test email",
            "<div>Hello from Archway.EmailClientImplIntegrationSpec</div>",
            "valhalla@phdata.io",
            "tony@phdata.io")
      .unsafeRunSync()
  }

  trait Context {
    val mailClient = new EmailClientImpl[IO](itestConfig, ExecutionContext.global)
  }
}
