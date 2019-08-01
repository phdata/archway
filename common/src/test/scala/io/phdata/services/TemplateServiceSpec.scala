package io.phdata.services

import io.phdata.models.TemplateRequest
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

class TemplateServiceSpec extends PropSpec with TableDrivenPropertyChecks with MockFactory with Matchers {

  val variations = Table(
    ("project name", "system name"),
    ("sesame", "sesame"),
    ("Sesame", "sesame"),
    ("Open Sesame", "open_sesame"),
    ("Sesame & Sons, LLC.", "sesame_sons_llc")
  )

  property("generateName property") {
    forAll(variations) { (input, expected) =>
      val actual = TemplateRequest.generateName(input)
      actual shouldBe expected
    }
  }

}

