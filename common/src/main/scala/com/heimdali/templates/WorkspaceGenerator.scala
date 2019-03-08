package com.heimdali.templates

import java.time.Clock

import cats.effect.Sync
import com.heimdali.config.{AppConfig, TemplateConfig}
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

  def instance[F[_], A <: WorkspaceGenerator[F, _]](appConfig: AppConfig, className: TemplateConfig => String)
                                                   (implicit clock: Clock, F: Sync[F]): A =
    Class
      .forName(className(appConfig.templates))
      .getConstructors
      .head
      .newInstance(appConfig, clock, F)
      .asInstanceOf[A]


}






