package com.heimdali.templates

import java.time.Clock

import cats.effect._
import com.heimdali.config.AppConfig
import com.heimdali.models.{User, UserTemplate, WorkspaceRequest}
import com.heimdali.test.fixtures._
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

class TemplateGeneratorSpec extends PropSpec with TableDrivenPropertyChecks with Matchers {

  val variations = Table(
    ("project name", "system name"),
    ("sesame", "sesame"),
    ("Sesame", "sesame"),
    ("Open Sesame", "open_sesame"),
    ("Sesame & Sons, LLC.", "sesame_sons_llc")
  )

  property("generateName property") {
    forAll(variations) { (input, expected) =>
      val actual = TemplateGenerator.generateName(input)
      actual shouldBe expected
    }
  }

  property("instance") {
    TemplateGenerator.instance[SyncIO, TemplateGenerator[SyncIO, UserTemplate]](appConfig.copy(templates = appConfig.templates.copy(userGenerator = classOf[TestTemplateGenerator[SyncIO]].getName)), _.userGenerator)
  }

}

