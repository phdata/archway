package com.heimdali.clients

import cats.effect.IO
import com.heimdali.services.{CDHClusterService, ClusterService}
import com.heimdali.test.fixtures._
import io.circe.parser._
import org.apache.hadoop.conf.Configuration
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.Queue
import scala.io.Source

class CDHYarnClientSpec extends FlatSpec with MockFactory with Matchers with HttpTest {

  behavior of "CDHYarnClientSpec"

  it should "createPool" in {
    val yarnClient = IO.pure(Client.fromHttpService(HttpService[IO] {
      case GET -> Root / "api" / "v18" / "clusters" / "cluster" / "services" / yarnApp.id / "config" =>
        Ok(fromResource("cloudera/config.json"))
      case PUT -> Root / "api" / "v18" / "clusters" / "cluster" / "services" / yarnApp.id / "config" =>
        Ok(fromResource("cloudera/config_update.json"))
      case POST -> Root / "api" / "v18" / "clusters" / "cluster" / "commands" / "poolsRefresh" =>
        Ok(fromResource("cloudera/commands.poolsRefresh.expected.json"))
    }))
    val yarnHttpClient = new CMClient[IO](yarnClient, clusterConfig)

    val clusterService = new CDHClusterService[IO](httpClient, clusterConfig, new Configuration())

    val client = new CDHYarnClient[IO](yarnHttpClient, clusterConfig, clusterService)
    client.createPool("root.pool", 1, 1).unsafeRunSync()
  }

  it should "evaluate the correct json" in {
    val Right(expected) = parse(Source.fromResource("cloudera/pool_json.json").getLines().mkString)

    val client = new CDHYarnClient(null, clusterConfig, mock[ClusterService[IO]])
    val result = client.config("test", 1, 1)
    result should be(expected)
  }

  it should "combine json" in {
    val Right(input) = parse(Source.fromResource("cloudera/pool.json").getLines().mkString)
    val Right(expected) = parse(Source.fromResource("cloudera/pool_after.json").getLines().mkString)

    val client = new CDHYarnClient(null, clusterConfig, mock[ClusterService[IO]])
    val result = client.combine(input, "test", 1, 1, Queue("root"))
    result should be(expected)
  }

  it should "find correct queue" in {
    val Right(json) = parse(Source.fromResource("cloudera/pool.json").getLines().mkString)
    val client = new CDHYarnClient(null, clusterConfig, mock[ClusterService[IO]])
    val result = client.dig(json.hcursor, Queue("root"))
    result.top shouldBe defined
  }

  it should "generate parent pools" in {
    val client = new CDHYarnClient(null, clusterConfig, mock[ClusterService[IO]])
    val result = client.getParents(poolName).unsafeRunSync()

    result.toList shouldBe List("root", "workspaces")
  }

}


