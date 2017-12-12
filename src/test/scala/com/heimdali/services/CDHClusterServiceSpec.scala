package com.heimdali.services

import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.{HttpConnectionContext, HttpExt, HttpsConnectionContext}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, ResponseEntity}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Printer
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalamock.scalatest.MockFactory
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

class CDHClusterServiceSpec
  extends AsyncFlatSpec
    with Matchers
    with ScalatestRouteTest
    with FailFastCirceSupport
    with MockFactory {

  behavior of "CDH Cluster service"

  it should "return a cluster" in {
    val url = ""
    val name = "odin"
    val version = "5.12.0"

    val username = "admin"
    val password = "admin"

    val configuration = ConfigFactory.defaultApplication()

    import io.circe.generic.auto._
    val Right(json) = io.circe.parser.parse(Source.fromResource("cloudera/cluster.json").getLines().mkString)
    val response = HttpResponse(akka.http.scaladsl.model.StatusCodes.OK).withEntity(json.pretty(Printer.spaces2))

    val http = mock[HttpExt]
    (http.singleRequest(_: HttpRequest)(_: Materializer))
        .expects(*, *)
        .returning(Future(response))

    val service = new CDHClusterService(http, configuration)(ExecutionContext.global, ActorMaterializer())
    service.list map { list =>
      list should have length 1
      list should be(Seq(Cluster(name, "Odin", CDH(version))))
    }
  }
}