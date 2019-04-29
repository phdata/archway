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
    val configService = mock[ConfigService[IO]]
    val ldapGroupGenerator = new DefaultLDAPGroupGenerator[IO](configService)
    val appGenerator = new DefaultApplicationGenerator[IO](appConfig, ldapGroupGenerator)
    val topicGenerator = new DefaultTopicGenerator[IO](appConfig, ldapGroupGenerator)

    WorkspaceGenerator.instance[IO, WorkspaceGenerator[IO, UserTemplate]](appConfig.copy(generators = appConfig.generators.copy(userGenerator = classOf[TestWorkspaceGenerator[IO]].getName)), ldapGroupGenerator, appGenerator, topicGenerator, _.userGenerator)
  }

}

