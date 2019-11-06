package io.phdata.services

import cats.effect.IO
import org.scalatest.{FlatSpec, Matchers}

class FeatureServiceImplSpec extends FlatSpec with Matchers {

  behavior of "Feature Service"

  it should "return true if feature is enabled" in new Context{
    featureService.isEnabled("enabled-feature") shouldBe true
  }

  it should "return false if feature is not enabled" in new Context{
    featureService.isEnabled("not-enabled-feature") shouldBe false
  }

  it should "return all enabled features" in new Context {
    val result = featureService.all()

    result.size shouldBe 2
    result shouldBe Seq("ENABLED-FEATURE", "ENABLED-FEATURE-2")
  }

  trait Context {
    val featureService = new FeatureServiceImpl[IO]("ENABLED-FEATURE,ENABLED-FEATURE-2")
  }

}
