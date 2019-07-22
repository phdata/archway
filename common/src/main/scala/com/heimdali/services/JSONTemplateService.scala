package com.heimdali.services

import java.nio.file.{FileVisitOption, Files, Path, Paths}
import java.time.Instant
import java.util.concurrent.TimeUnit

import cats.effect._
import cats.effect.implicits._
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.models.{Compliance, TemplateRequest, User, WorkspaceRequest}
import com.typesafe.scalalogging.LazyLogging
import org.fusesource.scalate.TemplateEngine

import scala.collection.JavaConverters._

class JSONTemplateService[F[_]: Effect: Clock](context: AppContext[F], configService: ConfigService[F])
    extends TemplateService[F] with LazyLogging {

  val templateEngine = new TemplateEngine()

  override def defaults(user: User): F[TemplateRequest] =
    TemplateRequest(user.username, user.name, user.name, Compliance.empty, user.distinguishedName).pure[F]

  private[services] def generateJSON(template: TemplateRequest, templatePath: String, templateName: String): F[String] =
    Sync[F].delay {
      logger.info("Using template path {}", templatePath)
      templateEngine.layout(
        templatePath,
        Map(
          "templateName" -> templateName,
          "appConfig" -> context.appConfig,
          "nextGid" -> (() => configService.getAndSetNextGid.toIO.unsafeRunSync()),
          "template" -> template
            .copy(requester = template.requester.replace("""\""", """\\""")) // Handle backslash in a user DN
        )
      )
    }

  override def workspaceFor(template: TemplateRequest, templateName: String): F[WorkspaceRequest] = {
    val templateRootPath = Paths.get(context.appConfig.templates.templateRoot, s"$templateName.ssp")

    val templatePath = if (templateRootPath.toFile.exists) {
      templateRootPath
    } else {
      Paths.get(context.appConfig.templates.templateRoot, "custom", s"$templateName.ssp")
    }

    logger.info("generating {} from {}", templateName, templatePath)
    generateWorkspaceRequest(template, templatePath.toString, templateName)
  }

  override def customTemplates: F[List[WorkspaceRequest]] = {
    val templatesPath: Path = Paths.get(context.appConfig.templates.templateRoot, "custom")

    if (templatesPath.toFile.exists) {
      val customTemplates = Files
        .walk(templatesPath, 1, FileVisitOption.FOLLOW_LINKS)
        .iterator()
        .asScala
        .toList
        .map(_.toString)
        .filter(_.endsWith(".ssp"))
        .sorted

      // creating empty object because it is not needed only required
      val templateRequest: TemplateRequest = TemplateRequest("", "", "", Compliance(false, false, false), "")

      customTemplates.traverse[F, WorkspaceRequest] { templatePath =>
        generateWorkspaceRequest(templateRequest, templatePath, extractTemplateName(templatePath))
      }
    } else {
      List.empty[WorkspaceRequest].pure[F]
    }
  }

  private def generateWorkspaceRequest(templateRequest: TemplateRequest,
                                       templatePath: String,
                                       templateName: String): F[WorkspaceRequest] = {
    // remove the escape sequences added for json parsing
    for {
      workspaceText <- generateJSON(templateRequest, templatePath, templateName)
      _ <- logger.debug("generated this output with the {} template: {}", templateName, workspaceText).pure[F]
      time <- Clock[F].realTime(TimeUnit.MILLISECONDS)
      _ <- logger.trace("Parsing workspace text: \n" + workspaceText).pure[F]
      Right(json) = io.circe.parser.parse(workspaceText)
      Right(result) = json.as[WorkspaceRequest](
        WorkspaceRequest.decoder(templateRequest.requester, Instant.ofEpochMilli(time))
      )
    } yield result
  }

  private def extractTemplateName(templatePath: String): String = {
    val fileName = Paths.get(templatePath).getFileName.toString
    fileName.split("\\.").toList.head
  }

}
