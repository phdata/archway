package com.heimdali.clients

import cats.effect.IO
import org.scalatest.FlatSpec
import com.heimdali.itest.fixtures._

import scala.concurrent.ExecutionContext

class EmailClientImplIntegrationSpec extends FlatSpec {
  it should "Send an email" in new Context {
    System.setProperty("mail.debug", "true")

    val result = mailClient
      .send("test email",
            "<div>Hello from Heimdali.EmailClientImplIntegrationSpec</div>",
            "valhalla@phdata.io",
            "tony@phdata.io")
      .unsafeRunSync()
  }

  trait Context {
    val mailClient = new EmailClientImpl[IO](itestConfig, ExecutionContext.global)
  }
}
