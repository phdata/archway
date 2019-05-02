package com.heimdali.services

import java.nio.file.Paths
import java.time.Instant
import java.util.concurrent.TimeUnit

import cats.effect._
import cats.effect.implicits._
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models.{Compliance, TemplateRequest, User, WorkspaceRequest}
import io.circe.Printer
import org.fusesource.scalate.{TemplateEngine, TemplateSource}

class JSONTemplateService[F[_]: Effect : Clock](appConfig: AppConfig,
                                                fileReader: FileReader[F],
                                                configService: ConfigService[F])
  extends TemplateService[F] {

  val templateEngine = new TemplateEngine()

  override def defaults(user: User): F[TemplateRequest] =
    TemplateRequest(user.name, user.name, user.name, Compliance.empty, user.distinguishedName).pure[F]

  private[services] def generateJSON(template: TemplateRequest, templateName: String, templateContent: String): F[String] =
    Sync[F].delay {
      val templatePath = s"$templateName.ssp"
      val templateItem = templateEngine.load(TemplateSource.fromText(templatePath, templateContent))
      templateEngine.layout(templatePath, templateItem, Map(
        "templateName" -> templateName,
        "appConfig" -> appConfig,
        "nextGid" -> (() => configService.getAndSetNextGid.toIO.unsafeRunSync()),
        "template" -> template
      ))
    }

  override def workspaceFor(template: TemplateRequest, templateName: String): F[WorkspaceRequest] =
    for {
      templateContent <- fileReader.readLines(Paths.get(appConfig.templates.templateRoot, s"$templateName.ssp").toString).map(_.mkString)
      workspaceText <- generateJSON(template, templateName, templateContent)
      time <- Clock[F].realTime(TimeUnit.MILLISECONDS)
      Right(json) = io.circe.parser.parse(workspaceText)
      _ <- println(json.pretty(Printer.spaces2)).pure[F]
      Right(result) = json.as[WorkspaceRequest](WorkspaceRequest.decoder(template.requester, Instant.ofEpochMilli(time)))
    } yield result

}
