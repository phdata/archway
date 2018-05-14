package com.heimdali.clients

import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.heimdali.services.{BasicClusterApp, ClusterService}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.parser._
import io.circe.{ACursor, Json}
import org.apache.http.HttpException

import scala.collection.immutable.Queue
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

case class YarnPool(name: String, maxCores: Int, maxMemoryInGB: Double)

trait YarnClient {
  def createPool(name: String, maxCores: Int, maxMemoryInGB: Double, parentPools: Queue[String]): Future[YarnPool]
}

class CDHYarnClient(http: HttpClient,
                    configuration: Config,
                    clusterService: ClusterService)
                   (implicit executionContext: ExecutionContext,
                    materializer: Materializer)
  extends YarnClient
    with FailFastCirceSupport
    with LazyLogging {

  val clusterConfig: Config = configuration.getConfig("cluster")
  val cluster: String = clusterConfig.getString("name")
  val baseUrl: String = clusterConfig.getString("url")
  val adminConfig: Config = clusterConfig.getConfig("admin")
  val username: String = adminConfig.getString("username")
  val password: String = adminConfig.getString("password")

  lazy val configURL: Future[String] = {
    clusterService.list.map { clusterList =>
      clusterList
        .find(_.id == cluster)
        .map { activeCluster =>
          val BasicClusterApp(yarn, _, _, _) = activeCluster.clusterApps("YARN")
          s"$baseUrl/clusters/$cluster/services/$yarn/config"
        }.get
    }
  }

  val refreshURL = s"$baseUrl/clusters/$cluster/commands/poolsRefresh"

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

  def dig(cursor: ACursor, parents: Queue[String]): ACursor = {
    parents.dequeueOption.map {
      case (parent, newQueue) =>
        val newCursor = cursor.downField("queues")
          .downAt(_.asObject.get.filter {
            case ("name", json) => parent == json.asString.get
            case _ => false
          }.nonEmpty)
        dig(newCursor, newQueue)
    }.getOrElse(cursor)
  }

  def combine(existing: Json, pool: YarnPool, parents: Queue[String]): Json = {
    logger.debug("adding {} to {}", pool, existing)
    val result = dig(existing.hcursor, parents)
      .downField("queues")
      .withFocus(_.withArray(arr => Json.arr(arr :+ config(pool): _*)))
      .top.get
    logger.debug("new pool configuration: {}", result)
    result
  }

  def prepare(container: Json, newConfig: Json): Json = {
    import io.circe.optics.JsonPath._
    container.hcursor
      .downField("items")
      .withFocus {
        _.mapArray { arr =>
          arr.map {
            case json if root.name.string.getOption(json).get.contains("yarn_fs_scheduled_allocations") =>
              logger.debug(root.value.string.modify(_ => newConfig.toString())(json).toString())
              root.value.string.modify(_ => newConfig.toString())(json)
            case json =>
              json
          }
        }
      }.top.get
  }

  def yarnConfig: Future[Json] = {
    configURL.flatMap { url =>
      val request = Get(url).addCredentials(BasicHttpCredentials(username, password))
      http.request(request)
        .flatMap {
          case HttpResponse(StatusCodes.OK, _, entity, _) =>
            val response = Await.result(Unmarshal(entity).to[Json], Duration.Inf)
            logger.debug("configuration looks like: {}", response)
            Future(response)
          case HttpResponse(status, _, entity, _) =>
            Unmarshal(entity).to[String].map { result =>
              logger.error("{} couldn't call resource pool configuration: {}", status, result)
              throw new HttpException()
            }
        }
        .recover {
          case ex =>
            logger.error("couldn't get resource pool configuration: {}", ex)
            throw ex
        }
    }
  }

  def yarnConfigUpdate(updatedJson: Json): Future[Json] = {
    configURL flatMap { url =>
      val request = Put(url, updatedJson).addCredentials(BasicHttpCredentials(username, password))
      http.request(request)
        .flatMap {
          case HttpResponse(StatusCodes.OK, _, entity, _) =>
            Unmarshal(entity).to[Json].map { result =>
              logger.debug("config update resulted with {}", result)
              result
            }
          case HttpResponse(status, _, entity, _) =>
            Unmarshal(entity).to[String].map { result =>
              logger.error("{} couldn't update resource pool configuration: {}", status, result)
              throw new HttpException()
            }
        }
        .recover {
          case ex =>
            logger.error("couldn't update resource pool configuration: {}", ex)
            throw ex
        }
    }
  }

  def yarnConfigRefresh: Future[Json] = {
    val request = Post(refreshURL)
      .addCredentials(BasicHttpCredentials(username, password))
    http.request(request)
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          Unmarshal(entity).to[Json].map { result =>
            logger.debug("config refresh resulted with {}", result)
            result
          }
        case HttpResponse(status, _, entity, _) =>
          Unmarshal(entity).to[String].map { result =>
            logger.error("{} couldn't update resource pool configuration: {}", status, result)
            throw new HttpException()
          }
      }
      .recover {
        case ex =>
          logger.error("couldn't refresh resource pool configuration: {}", ex)
          throw ex
      }
  }


  override def createPool(poolName: String, maxCores: Int, maxMemoryInGB: Double, parentPools: Queue[String]): Future[YarnPool] = {
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

      val updatedJson = combine(configJson, yarnPool, parentPools)

      val preparedJson = prepare(config, updatedJson)
      for {
        _ <- yarnConfigUpdate(preparedJson)
        _ <- yarnConfigRefresh
      } yield yarnPool
    }
  }
}
