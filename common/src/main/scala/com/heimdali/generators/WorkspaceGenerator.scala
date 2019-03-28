package com.heimdali.generators

import cats.effect.{Clock, Sync}
import com.heimdali.config.{AppConfig, GeneratorConfig}
import com.heimdali.models._

trait WorkspaceGenerator[F[_], A] {

  def defaults(user: User): F[A]

  def workspaceFor(template: A): F[WorkspaceRequest]

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






