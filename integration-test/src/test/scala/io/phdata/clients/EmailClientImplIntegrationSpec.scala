package io.phdata.clients

import cats.effect.IO
import org.scalatest.FlatSpec
import io.phdata.itest.fixtures._
import org.fusesource.scalate.TemplateEngine

import scala.concurrent.ExecutionContext

class EmailClientImplIntegrationSpec extends FlatSpec {
  it should "Send an email" in new Context {
    System.setProperty("mail.debug", "true")

    val result = mailClient
      .send("test email",
            "<div>Hello from Archway.EmailClientImplIntegrationSpec</div>",
            "valhalla@phdata.io",
            "rkaland@phdata.io")
      .unsafeRunSync()
  }

  it should "Send an welcome email" in new Context {
    System.setProperty("mail.debug", "true")

    val htmlContent = templateEngine.layout(
      "/templates/emails/welcome.mustache",
      Map(
        "roleName" -> "manager",
        "resourceType" -> "data",
        "workspaceName" -> "Test workspace",
        "uiUrl" -> "https://edge1.valhalla.phdata.io:8080",
        "workspaceId" -> "123"
      )
    )

    val result = mailClient
      .send("Test welcome email",
        htmlContent,
        "valhalla@phdata.io",
        itestConfig.approvers.notificationEmail.head)
      .unsafeRunSync()
  }

  trait Context {
    val mailClient = new EmailClientImpl[IO](itestConfig, ExecutionContext.global)
    val templateEngine: TemplateEngine = new TemplateEngine()
  }
}
