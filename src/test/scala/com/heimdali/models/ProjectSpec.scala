package com.heimdali.models

import com.heimdali.test.fixtures.TestProject
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

class ProjectSpec extends PropSpec with Matchers with TableDrivenPropertyChecks {

  val variations = Table(
    ("project name", "system name"),
    ("sesame", "sesame"),
    ("Sesame", "sesame"),
    ("Open Sesame", "open_sesame"),
    ("Sesame & Sons, LLC.", "sesame_sons_llc")
  )

  property("a project's system name should replace illegal characters") {
    forAll(variations) { (projectName, systemName) =>
      val project = TestProject(name = projectName)
      project.generatedName should be (systemName)
    }
  }

}
