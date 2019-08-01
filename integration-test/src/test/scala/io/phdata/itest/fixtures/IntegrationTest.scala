/* Copyright 2018 phData Inc. */

package io.phdata.itest.fixtures

import org.scalatest.TestSuite
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar._

trait IntegrationTest extends TestSuite with TimeLimitedTests {

  val timeLimit = 60000 millis
}

