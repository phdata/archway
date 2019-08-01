package io.phdata.test.fixtures

import cats.effect.IO
import org.http4s.{EntityDecoder, Response, Status}
import org.scalatest.{Assertion, Matchers}

trait HttpTest { this: Matchers =>

  def check[A](actual: IO[Response[IO]],
               expectedStatus: Status,
               expectedBody: Option[A])(
                implicit ev: EntityDecoder[IO, A]
              ): Assertion = {
    val actualResp = actual.unsafeRunSync
    actualResp.status should be (expectedStatus)
    expectedBody.fold[Assertion](
      actualResp.body.compile.toVector.unsafeRunSync shouldBe empty)(
      expected => actualResp.as[A].unsafeRunSync shouldBe expected
    )
  }
}
