package com.heimdali.services

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.{HttpExt, HttpsConnectionContext}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import io.circe.parser._

import scala.collection.JavaConverters._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito._
import org.scalatest.{AsyncFlatSpec, FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

class CDHYarnClientSpec extends AsyncFlatSpec with MockitoSugar with Matchers with FailFastCirceSupport {

  behavior of "CDHYarnClientSpec"

  it should "createPool" in {
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

    val configUrl = "/services/yarn/config"
    val refreshUrl = "/commands/poolsRefresh"
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

    val cdhClient = mock[HttpExt]

    when(cdhClient.singleRequest(any[HttpRequest], any[HttpsConnectionContext], any[ConnectionPoolSettings], any[LoggingAdapter])(any[Materializer])).thenReturn(
      Future(HttpResponse(StatusCodes.OK, entity = initialEntity)),
      Future(HttpResponse(StatusCodes.OK, entity = updateEntity)),
      Future(HttpResponse(StatusCodes.OK, entity = refreshEntity))
    )

    val client = new CDHYarnClient(cdhClient, configuration)
    client.createPool("pool", 1, 1.0).map { result =>
      val capture = ArgumentCaptor.forClass(classOf[HttpRequest])
      verify(cdhClient, times(3)).singleRequest(capture.capture(), any[HttpsConnectionContext], any[ConnectionPoolSettings], any[LoggingAdapter])(any[Materializer])

      capture.getAllValues.asScala.map(r => r.method -> r.uri.toString()).toList should be (Seq(
        HttpMethods.GET -> configUrl,
        HttpMethods.PUT -> configUrl,
        HttpMethods.GET -> refreshUrl
      ))
    }
  }

  it should "evaluate the correct json" in {
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

    val Right(expected) = parse(Source.fromResource("cloudera/pool_json.json").getLines().mkString)

    val client = new CDHYarnClient(mock[HttpExt], ConfigFactory.load())
    val result = client.config(YarnPool("test", 1, 1.0))
    result should be(expected)
  }

  it should "combine json" in {
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

    val Right(input) = parse(Source.fromResource("cloudera/pool.json").getLines().mkString)
    val Right(expected) = parse(Source.fromResource("cloudera/pool_after.json").getLines().mkString)

    val client = new CDHYarnClient(mock[HttpExt], ConfigFactory.load())
    val result = client.combine(input, YarnPool("test", 1, 1.0))
    result should be(expected)
  }

}
