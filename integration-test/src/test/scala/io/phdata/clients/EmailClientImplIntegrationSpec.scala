package io.phdata.clients


import cats.effect.IO
import courier.Multipart
import io.phdata.itest.fixtures._
import io.phdata.test.fixtures._
import io.phdata.services.EmbeddedImageEmail
import org.fusesource.scalate.TemplateEngine
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext

class EmailClientImplIntegrationSpec extends FlatSpec {
  it should "Send an email" in new Context {
    System.setProperty("mail.debug", "true")

    val result = mailClient
      .send("test email",
        Multipart().html("<div>Hello from Archway.EmailClientImplIntegrationSpec</div>"),
        appConfig.smtp.fromEmail,
        itestConfig.approvers.notificationEmail.head)
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
        Multipart().html(htmlContent),
        appConfig.smtp.fromEmail,
        itestConfig.approvers.notificationEmail.head)
      .unsafeRunSync()
  }

  it should "Send a new workspace email" in new Context{
    System.setProperty("mail.debug", "true")

    val htmlContent = templateEngine.layout(
      "common/src/main/templates/emails/incoming.mustache",
      Map(
        "roleName" -> "manager",
        "resourceType" -> "data",
        "workspaceName" -> "Test workspace",
        "uiUrl" -> "https://edge1.valhalla.phdata.io:8080",
        "workspaceId" -> "123",
        "userName" -> "TestUser"
      )
    )
    val result = mailClient
      .send("Test new workspace email",
        EmbeddedImageEmail.create(htmlContent, "images/logo_big.png", "logo"),
        appConfig.smtp.fromEmail,
        itestConfig.approvers.notificationEmail.head)
      .unsafeRunSync()
  }

  trait Context {
    val mailClient = new EmailClientImpl[IO](itestConfig, ExecutionContext.global)
    val templateEngine: TemplateEngine = new TemplateEngine()
  }
}
