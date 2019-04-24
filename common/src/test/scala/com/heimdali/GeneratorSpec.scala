package com.heimdali

import org.fusesource.scalate.TemplateEngine
import org.scalatest.{FlatSpec, FunSpec}
import com.heimdali.test.fixtures._

class GeneratorSpec extends FunSpec {

  describe("something") {

    it("should generate") {
      val templateEngine = new TemplateEngine()
      val result = templateEngine.layout(getClass.getResource("/json.ssp").getPath, Map("workspace" -> savedWorkspaceRequest))
      println(result)
    }

  }

}