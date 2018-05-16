package com.heimdali.clients

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.stream.ActorMaterializer
import com.heimdali.services.{BasicClusterApp, CDH, Cluster, ClusterService}
import com.heimdali.test.fixtures._
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import io.circe.parser._
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.collection.immutable.Queue
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

class CDHYarnClientSpec extends AsyncFlatSpec with AsyncMockFactory with Matchers with FailFastCirceSupport {

  behavior of "CDHYarnClientSpec"

  it should "createPool" in {
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

    val configUrl = s"/clusters/cluster%20name/services/${yarnApp.id}/config"
    val refreshUrl = "/clusters/cluster%20name/commands/poolsRefresh"
    val initialJsonString: String = Source.fromResource("cloudera/config.json").getLines().mkString
    val Right(initialJson) = parse(initialJsonString)
    val updateJsonString: String = Source.fromResource("cloudera/config_update.json").getLines().mkString
    val Right(updateJson) = parse(updateJsonString)
    val refreshResponseJson = Json.obj(
      "id" -> Json.fromLong(123),
      "name" -> Json.fromString("RefreshPools"),
      "startTime" -> Json.fromString(LocalDateTime.now().format(ISO_LOCAL_DATE_TIME)),
      "active" -> Json.fromBoolean(true),
      "clusterRef" -> Json.obj(
        "clusterName" -> Json.fromString("cluster")
      )
    )

    val configuration = ConfigFactory.defaultApplication()

    val initialEntity: ResponseEntity = Await.result(Marshal(initialJson).to[ResponseEntity], Duration.Inf)
    val updateEntity: ResponseEntity = Await.result(Marshal(updateJson).to[ResponseEntity], Duration.Inf)
    val refreshEntity: ResponseEntity = Await.result(Marshal(refreshResponseJson).to[ResponseEntity], Duration.Inf)

    val cdhClient = mock[HttpClient]
    (cdhClient.request _).expects(Get(configUrl).addCredentials(BasicHttpCredentials("admin", "admin"))).returning(Future(HttpResponse(StatusCodes.OK, entity = initialEntity)))
    cdhClient.request _ expects where {
      request: HttpRequest => request.uri.toString() == configUrl && request.method == HttpMethods.PUT
    } returning Future(HttpResponse(StatusCodes.OK, entity = updateEntity))
    (cdhClient.request _).expects(Post(refreshUrl).addCredentials(BasicHttpCredentials("admin", "admin"))).returning(Future(HttpResponse(StatusCodes.OK, entity = refreshEntity)))

    val clusterService = mock[ClusterService]
    clusterService.list _ expects() returning Future(Seq(cluster))

    val client = new CDHYarnClient(cdhClient, configuration, clusterService)
    client.createPool("pool", 1, 1.0, Queue("root")).map { result =>
      result.name should be ("pool")
    }
  }

  it should "evaluate the correct json" in {
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

    val Right(expected) = parse(Source.fromResource("cloudera/pool_json.json").getLines().mkString)

    val client = new CDHYarnClient(mock[HttpClient], ConfigFactory.load(), mock[ClusterService])
    val result = client.config(YarnPool("test", 1, 1.0))
    result should be(expected)
  }

  it should "combine json" in {
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

    val Right(input) = parse(Source.fromResource("cloudera/pool.json").getLines().mkString)
    val Right(expected) = parse(Source.fromResource("cloudera/pool_after.json").getLines().mkString)

    val client = new CDHYarnClient(mock[HttpClient], ConfigFactory.load(), mock[ClusterService])
    val result = client.combine(input, YarnPool("test", 1, 1.0), Queue("root"))
    result should be(expected)
  }

}
