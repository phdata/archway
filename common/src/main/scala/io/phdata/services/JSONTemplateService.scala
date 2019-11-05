package io.phdata.services

import java.nio.file.{FileVisitOption, Files, Path, Paths}
import java.time.Instant
import java.util.concurrent.TimeUnit

import cats.effect._
import cats.effect.implicits._
import cats.implicits._
import io.phdata.AppContext
import io.phdata.models.{ComplianceQuestion, DistinguishedName, TemplateRequest, User, WorkspaceRequest}
import com.typesafe.scalalogging.LazyLogging
import org.fusesource.scalate.TemplateEngine

import scala.collection.JavaConverters._

class JSONTemplateService[F[_]: Effect: Clock](context: AppContext[F], configService: ConfigService[F])
    extends TemplateService[F] with LazyLogging {

  val templateEngine = new TemplateEngine()

  private val customTemplatesData: F[List[(WorkspaceRequest, String)]] = {
    val customTemplatesPath: Path = Paths.get(context.appConfig.templates.templateRoot, "custom")

    if (customTemplatesPath.toFile.exists) {
      val customTemplates = Files
        .walk(customTemplatesPath, 1, FileVisitOption.FOLLOW_LINKS)
        .iterator()
        .asScala
        .toList
        .map(_.toString)
        .filter(_.endsWith(".ssp"))
        .sorted

      // creating empty object because it is not needed only required
      val templateRequest: TemplateRequest =
        TemplateRequest(
          "",
          "",
          "",
          List.empty,
          DistinguishedName("cn=admin,ou=heimdali,dc=io"),
          false
        )

      customTemplates.traverse[F, (WorkspaceRequest, String)] { templatePath =>
        generateWorkspaceRequest(templateRequest, templatePath, extractTemplateName(templatePath))
          .map(workspaceRequest => (workspaceRequest, templatePath))
      }
    } else {
      List.empty[(WorkspaceRequest, String)].pure[F]
    }
  }

  override def defaults(user: User): F[TemplateRequest] =
    TemplateRequest(user.username, user.name, user.name, List.empty, user.distinguishedName).pure[F]

  private[services] def generateJSON(template: TemplateRequest, templatePath: String, templateName: String): F[String] =
    Sync[F].delay {
      logger.info("Using template path {}", templatePath)
      templateEngine.layout(
        templatePath,
        Map(
          "templateName" -> templateName,
          "appConfig" -> context.appConfig,
          "nextGid" -> (() => if (template.generateNextId) configService.getAndSetNextGid.toIO.unsafeRunSync() else 0),
          "template" -> template.copy(
                requester = DistinguishedName(template.requester.value.replace("""\""", """\\"""))
              ) // Handle backslash in a user DN
        )
      )
    }

  override def workspaceFor(template: TemplateRequest, templateName: String): F[WorkspaceRequest] = {

    if (Seq("user", "simple", "structured").contains(templateName)) {
      val templatePath = Paths.get(context.appConfig.templates.templateRoot, s"$templateName.ssp")

      logger.info("generating {} from {}", templateName, templatePath)
      generateWorkspaceRequest(template, templatePath.toString, templateName)
    } else {
      for {
        data <- customTemplatesData
        templateInfo <- data
          .find(_._1.metadata.name == templateName)
          .getOrElse(
            throw new Exception(
              s"Template with name $templateName could not be found at " +
                  s"${Paths.get(context.appConfig.templates.templateRoot, "custom")})"
            )
          )
          .pure[F]
        _ <- logger.info("generating {} from {}", templateName, templateInfo._2).pure[F]
        result <- generateWorkspaceRequest(template, templateInfo._2, templateName)
      } yield result
    }
  }

  override def customTemplates: F[List[WorkspaceRequest]] = customTemplatesData.map(_.map(_._1))

  private def generateWorkspaceRequest(
      templateRequest: TemplateRequest,
      templatePath: String,
      templateName: String
  ): F[WorkspaceRequest] = {
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

  def verifyDefaultTemplates: F[Unit] = {
    val defaultCompliance = List(
      ComplianceQuestion("Full or partial credit card numbers?", "Joe", Instant.now(), Some(123L), Some(1))
    )

    val simpleTemplateRequest =
      TemplateRequest(
        "simple",
        "simple-summary",
        "simple-description",
        defaultCompliance,
        DistinguishedName("cn=admin,ou=heimdali,dc=io"),
        false
      )
    val userTemplateRequest = TemplateRequest(
      "user",
      "user-summary",
      "user-description",
      defaultCompliance,
      DistinguishedName("cn=admin,ou=heimdali,dc=io"),
      false
    )
    val structuredTemplateRequest =
      TemplateRequest(
        "structured",
        "structured-summary",
        "structured-description",
        defaultCompliance,
        DistinguishedName("cn=admin,ou=heimdali,dc=io"),
        false
      )

    val defaultTemplatesRequests = List(simpleTemplateRequest, userTemplateRequest, structuredTemplateRequest)

    for {
      _ <- defaultTemplatesRequests.traverse(templateRequest => workspaceFor(templateRequest, templateRequest.name))
    } yield ()
  }

  def verifyCustomTemplates: F[List[(WorkspaceRequest, String)]] = customTemplatesData

  private def extractTemplateName(templatePath: String): String = {
    val fileName = Paths.get(templatePath).getFileName.toString
    fileName.split("\\.").toList.head
  }

}
