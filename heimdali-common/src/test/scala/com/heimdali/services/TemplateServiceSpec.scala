package com.heimdali.services

import cats.effect.IO
import com.heimdali.models._
import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.TableDrivenPropertyChecks

class TemplateServiceSpec extends PropSpec with TableDrivenPropertyChecks with Matchers {

  val variations = Table(
    ("project name", "system name"),
    ("sesame", "sesame"),
    ("Sesame", "sesame"),
    ("Open Sesame", "open_sesame"),
    ("Sesame & Sons, LLC.", "sesame_sons_llc")
  )

  property("generateName property") {
    val templateService = new TemplateService[IO] {
      override def userDefaults(user: User): IO[UserTemplate] = ???
      override def userWorkspace(userTemplate: UserTemplate): IO[WorkspaceRequest] = ???
      override def simpleDefaults(user: User): IO[SimpleTemplate] = ???
      override def simpleWorkspace(simpleTemplate: SimpleTemplate): IO[WorkspaceRequest] = ???
      override def structuredDefaults(user: User): IO[StructuredTemplate] = ???
      override def structuredWorkspace(structuredTemplate: StructuredTemplate): IO[WorkspaceRequest] = ???
    }

    forAll(variations) { (input, expected) =>
      val actual = templateService.generateName(input)
      actual shouldBe expected
    }
  }

}
