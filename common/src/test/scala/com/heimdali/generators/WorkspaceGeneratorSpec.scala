package com.heimdali.generators

import java.time.Clock

import cats.effect._
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models.{User, UserTemplate, WorkspaceRequest}
import com.heimdali.services.ConfigService
import com.heimdali.test.fixtures._
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

class WorkspaceGeneratorSpec extends PropSpec with TableDrivenPropertyChecks with MockFactory with Matchers {

  val variations = Table(
    ("project name", "system name"),
    ("sesame", "sesame"),
    ("Sesame", "sesame"),
    ("Open Sesame", "open_sesame"),
    ("Sesame & Sons, LLC.", "sesame_sons_llc")
  )

  property("generateName property") {
    forAll(variations) { (input, expected) =>
      val actual = WorkspaceGenerator.generateName(input)
      actual shouldBe expected
    }
  }

  property("instance") {
    val configService = mock[ConfigService[SyncIO]]
    val ldapGroupGenerator = new DefaultLDAPGroupGenerator[SyncIO](appConfig, configService)
    val appGenerator = new DefaultApplicationGenerator[SyncIO](appConfig, ldapGroupGenerator)
    WorkspaceGenerator.instance[SyncIO, WorkspaceGenerator[SyncIO, UserTemplate]](appConfig.copy(generators = appConfig.generators.copy(userGenerator = classOf[TestWorkspaceGenerator[SyncIO]].getName)), ldapGroupGenerator, appGenerator, _.userGenerator)
  }

}

