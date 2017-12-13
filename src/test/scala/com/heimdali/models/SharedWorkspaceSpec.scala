package com.heimdali.models

import com.heimdali.models.ViewModel.SharedWorkspace
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

class SharedWorkspaceSpec extends PropSpec with Matchers with TableDrivenPropertyChecks {

  val variations = Table(
    ("project name", "system name"),
    ("sesame", "sesame"),
    ("Sesame", "sesame"),
    ("Open Sesame", "open_sesame"),
    ("Sesame & Sons, LLC.", "sesame_sons_llc")
  )

  property("a project's system name should replace illegal characters") {
    forAll(variations) { (projectName, systemName) =>
      SharedWorkspace.generateName(projectName) should be (systemName)
    }
  }

}
