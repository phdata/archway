package com.heimdali.clients

import cats.effect.IO
import com.heimdali.config.{ClusterConfig, CredentialsConfig}
import com.heimdali.models.Yarn
import com.heimdali.services.ClusterService
import com.heimdali.test.fixtures._
import io.circe.Json
import io.circe.parser._
import org.http4s.{EntityDecoder, HttpService, Response, Status}
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Assertion, FlatSpec, Matchers}

import scala.collection.immutable.Queue
import scala.concurrent.duration._
import scala.io.Source

class CDHYarnClientSpec extends FlatSpec with MockFactory with Matchers with HttpTest {

  behavior of "CDHYarnClientSpec"

  it should "createPool" in {
    val testClient = IO.pure(Client.fromHttpService(HttpService[IO] {
      case GET -> Root / "clusters" / "cluster name" / "services" / yarnApp.id / "config" =>
        Ok(fromResource("cloudera/config.json"))
      case PUT -> Root / "clusters" / "cluster name" / "services" / yarnApp.id / "config" =>
        Ok(fromResource("cloudera/config_update.json"))
      case POST -> Root / "clusters" / "cluster name" / "commands" / "poolsRefresh" =>
        Ok(fromResource("cloudera/commands.poolsRefresh.expected.json"))
    }))
    val httpClient = new CMClient[IO](testClient, clusterConfig)

    val clusterService = mock[ClusterService[IO]]
    clusterService.list _ expects() returning IO(Seq(cluster))

    val client = new CDHYarnClient[IO](httpClient, clusterConfig, clusterService)
    client.createPool(Yarn("pool", 1, 1), Queue("root")).unsafeRunSync()
  }

  it should "evaluate the correct json" in {
    val Right(expected) = parse(Source.fromResource("cloudera/pool_json.json").getLines().mkString)

    val client = new CDHYarnClient(null, clusterConfig, mock[ClusterService[IO]])
    val result = client.config(Yarn("test", 1, 1))
    result should be(expected)
  }

  it should "combine json" in {
    val Right(input) = parse(Source.fromResource("cloudera/pool.json").getLines().mkString)
    val Right(expected) = parse(Source.fromResource("cloudera/pool_after.json").getLines().mkString)

    val client = new CDHYarnClient(null, clusterConfig, mock[ClusterService[IO]])
    val result = client.combine(input, Yarn("test", 1, 1), Queue("root"))
    result should be(expected)
  }

}

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

  def fromResource(path: String): Json = {
    val json = Source.fromResource(path).getLines().mkString
    val Right(parsedJson) = parse(json)
    parsedJson
  }
}