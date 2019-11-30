package io.phdata.generators

import java.nio.file.Paths

import cats.effect.Effect
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import io.phdata.AppContext
import io.phdata.models.{Application, DistinguishedName, TemplateRequest, WorkspaceRequest}
import io.phdata.services.ApplicationRequest
import org.fusesource.scalate.TemplateEngine

class JsonApplicationGenerator[F[_]: Effect](
    context: AppContext[F],
    ldapGroupGenerator: LDAPGroupGenerator[F]
) extends ApplicationGenerator[F] with LazyLogging {

  val templateEngine = new TemplateEngine()

  override def applicationFor(application: ApplicationRequest, workspace: WorkspaceRequest): F[Application] = {
    val templatePath = Paths.get(context.appConfig.templates.templateRoot, s"application.ssp")

    for {
      workspaceText <- generateJSON(application, workspace, templatePath.toString)
      _ <- logger.debug(s"generated json for $application").pure[F]
      _ <- logger.trace("Parsing workspace text: \n" + workspaceText).pure[F]
      Right(json) = io.circe.parser.parse(workspaceText)
      Right(result) = json.as[Application]
    } yield result
  }

  private def generateJSON(
      application: ApplicationRequest,
      workspace: WorkspaceRequest,
      templatePath: String
  ): F[String] = {
    val consumerGroup = s"${TemplateRequest.generateName(workspace.name)}_${application.name}_cg"

    ldapGroupGenerator
      .generate(
        consumerGroup,
        DistinguishedName(s"cn=$consumerGroup,${context.appConfig.ldap.groupPath}"),
        consumerGroup
      )
      .map { ldapGroup =>
        templateEngine.layout(
          templatePath,
          Map(
            "applicationName" -> application.name,
            "consumerGroup" -> consumerGroup,
            "group" -> ldapGroup
          )
        )
      }
  }
}
