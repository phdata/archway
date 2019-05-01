package com.heimdali.generators

import java.net.URI
import java.time.Instant
import java.util.concurrent.TimeUnit

import cats.Monad
import io.circe.syntax._
import io.circe.Decoder
import io.circe.generic.auto._
import cats.implicits._
import cats.effect.implicits._
import cats.effect._
import com.heimdali.config.{AppConfig, GeneratorConfig}
import com.heimdali.models._
import com.heimdali.services.ConfigService
import org.apache.commons.io.FilenameUtils
import org.fusesource.scalate.{Template, TemplateEngine, TemplateSource}

trait WorkspaceGenerator[F[_], A] {

  def defaults(user: User): F[A]

  def workspaceFor(template: A): F[WorkspaceRequest]

}

class JSONWorkspaceGenerator[F[_]: Effect : Clock](appConfig: AppConfig,
                                   configService: ConfigService[F])
  extends WorkspaceGenerator[F, TemplateRequest] {

  val templateEngine = new TemplateEngine()

  override def defaults(user: User): F[TemplateRequest] = ???

  private[generators] def generateJSON(template: TemplateRequest, templateContent: String): F[String] =
    Sync[F].delay {
      val templatePath = s"${template.templateName}.ssp"
      val templateItem = templateEngine.load(TemplateSource.fromText(templatePath, templateContent))
      templateEngine.layout(templatePath, templateItem, Map(
        "templateName" -> template.templateName,
        "appConfig" -> appConfig,
        "nextGid" -> (() => configService.getAndSetNextGid.toIO.unsafeRunSync()),
        "template" -> template
      ))
    }

  override def workspaceFor(template: TemplateRequest): F[WorkspaceRequest] =
    for {
      templateContent <- configService.getTemplate(template.templateName)
      workspaceText <- generateJSON(template, templateContent)
      time <- Clock[F].realTime(TimeUnit.MILLISECONDS)
      Right(json) = io.circe.parser.parse(workspaceText)
      Right(result) = json.as[WorkspaceRequest](WorkspaceRequest.decoder(template.requester, Instant.ofEpochMilli(time)))
    } yield result

}

object WorkspaceGenerator {

  def generateName(name: String): String =
    name
      .replaceAll("""[^a-zA-Z\d\s]""", " ") //replace with space to remove multiple underscores
      .trim //if we have leading or trailing spaces, clean them up
      .replaceAll("""\s+""", "_")
      .toLowerCase

  def instance[F[_], A <: WorkspaceGenerator[F, _]](appConfig: AppConfig,
                                                    ldapGroupGenerator: LDAPGroupGenerator[F],
                                                    applicationGenerator: ApplicationGenerator[F],
                                                    topicGenerator: TopicGenerator[F],
                                                    className: GeneratorConfig => String)
                                                   (implicit clock: Clock[F], F: Sync[F]): A =
    Class
      .forName(className(appConfig.generators))
      .getConstructors
      .head
      .newInstance(appConfig, ldapGroupGenerator, applicationGenerator, topicGenerator, clock, F)
      .asInstanceOf[A]


}






