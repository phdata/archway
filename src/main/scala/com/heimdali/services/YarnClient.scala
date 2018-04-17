package com.heimdali.services

import javax.inject.Inject

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import io.circe.parser._
import org.apache.http.HttpException

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

case class YarnPool(name: String, maxCores: Int, maxMemoryInGB: Double)

trait YarnClient {
  def createPool(name: String, maxCores: Int, maxMemoryInGB: Double): Future[YarnPool]
}

class CDHYarnClient(http: HttpClient,
                    configuration: Config)
                   (implicit executionContext: ExecutionContext,
                    materializer: Materializer)
  extends YarnClient with FailFastCirceSupport {

  val clusterConfig = configuration.getConfig("cluster")
  val baseUrl = clusterConfig.getString("url")
  val adminConfig = clusterConfig.getConfig("admin")
  val username = adminConfig.getString("username")
  val password = adminConfig.getString("password")

  val configURL = s"$baseUrl/services/yarn/config"
  val refreshURL = s"$baseUrl/commands/poolsRefresh"

  def config(pool: YarnPool): Json = Json.obj(
    "name" -> Json.fromString(pool.name),
    "schedulablePropertiesList" -> Json.arr(
      Json.obj(
        "maxResources" -> Json.obj(
          "memory" -> Json.fromDouble(pool.maxMemoryInGB * 1024).get,
          "vcores" -> Json.fromInt(pool.maxCores)
        ),
        "scheduleName" -> Json.fromString("default"),
        "weight" -> Json.fromDouble(1.0).get
      )
    ),
    "schedulingPolicy" -> Json.fromString("drf")
  )

  def combine(existing: Json, pool: YarnPool): Json = {
    existing.hcursor
      .downField("queues")
      .downAt(_.asObject.get.filter {
        case ("name", json) => "root" == json.asString.get
        case _ => false
      }.nonEmpty)
      .downField("queues")
      .withFocus(_.withArray(arr => Json.arr(arr :+ config(pool): _*)))
      .top.get
  }

  def yarnConfig: Future[Json] = {
    val request = Get(configURL).addCredentials(BasicHttpCredentials(username, password))
    http.request(request)
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          val response = Await.result(Unmarshal(entity).to[Json], Duration.Inf)
          Future(response)
        case HttpResponse(_, _, _, _) => Future.failed(new HttpException())
      }

  }

  def yarnConfigUpdate(updatedJson: Json): Future[Json] = {
    val request = Put(configURL, updatedJson).addCredentials(BasicHttpCredentials(username, password))
    http.request(request)
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) => Unmarshal(entity).to[Json]
        case HttpResponse(_, _, _, _) => Future.failed(new HttpException())
      }
  }

  def yarnConfigRefresh: Future[Json] = {
    val request = Get(refreshURL)
      .addCredentials(BasicHttpCredentials(username, password))
    http.request(request)
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) => Unmarshal(entity).to[Json]
        case HttpResponse(_, _, _, _) => Future.failed(new HttpException())
      }
  }


  override def createPool(poolName: String, maxCores: Int, maxMemoryInGB: Double): Future[YarnPool] = {
    val yarnPool = YarnPool(poolName, maxCores, maxMemoryInGB)
    yarnConfig.flatMap { config =>
      val poolConfiguration = config.hcursor
        .downField("items")
        .downAt {
          item =>
            item.asObject.get.filter { field =>
              field._1 == "name" && field._2.asString.contains("yarn_fs_scheduled_allocations")
            }.nonEmpty
        }.focus.get

      val Right(configJson) = parse((poolConfiguration \\ "value").head.asString.get)

      val updatedJson = combine(configJson, yarnPool)
      for {
        _ <- yarnConfigUpdate(updatedJson)
        _ <- yarnConfigRefresh
      } yield yarnPool
    }
  }
}
