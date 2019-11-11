package io.phdata.generators
import java.nio.file.Paths

import cats.effect.{Clock, Sync}
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import io.phdata.config.AppConfig
import io.phdata.models._
import org.fusesource.scalate.TemplateEngine

//TODO: LDAPGroupGenerator can be removed when we switch to this topic generator, now it's here only for backward compatibility
class JsonTopicGenerator[F[_]](appConfig: AppConfig, ldapGroupGenerator: LDAPGroupGenerator[F])(
    implicit clock: Clock[F],
    F: Sync[F]
) extends TopicGenerator[F] with LazyLogging {

  val templateEngine = new TemplateEngine()

  override def topicFor(
      name: String,
      partitions: Int,
      replicationFactor: Int,
      workspaceRequest: WorkspaceRequest
  ): F[KafkaTopic] = {
    val templatePath = Paths.get(appConfig.templates.templateRoot, "topic.ssp")

    generateTopic(name, partitions, replicationFactor, workspaceRequest, templatePath.toString)
  }

  private def generateTopic(
      name: String,
      partitions: Int,
      replicationFactor: Int,
      workspaceRequest: WorkspaceRequest,
      templatePath: String
  ): F[KafkaTopic] = {
    val workspaceSystemName = TemplateRequest.generateName(workspaceRequest.name)
    val topicSystemName = TemplateRequest.generateName(name)

    for {
      topicText <- generateJSON(templatePath, partitions, replicationFactor, workspaceRequest, name)
      _ <- logger
        .debug("generated this output with the {} topic: {}", templatePath, s"$workspaceSystemName.$topicSystemName")
        .pure[F]
      Right(json) = io.circe.parser.parse(topicText)
      Right(result) = json.as[KafkaTopic]
    } yield result
  }

  def generateJSON(
      templatePath: String,
      partitions: Int,
      replicationFactor: Int,
      workspaceRequest: WorkspaceRequest,
      name: String
  ): F[String] = {

    Sync[F].delay {
      logger.info("Using template path {}", templatePath)
      templateEngine.layout(
        templatePath,
        Map(
          "partitions" -> partitions,
          "replicationFactor" -> replicationFactor,
          "name" -> name,
          "workspaceRequestName" -> workspaceRequest.name,
          "ldapGroupPath" -> appConfig.ldap.groupPath
        )
      )
    }
  }

}
