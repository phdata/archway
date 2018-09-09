package com.heimdali.clients

import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.heimdali.config.ClusterConfig
import com.heimdali.services.ClusterService
import com.typesafe.scalalogging.LazyLogging
import io.circe.parser._
import io.circe.{ACursor, Json}
import org.http4s.Method._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl

import scala.collection.immutable.Queue

case class YarnPool(name: String, maxCores: Int, maxMemoryInGB: Double)

trait YarnClient[F[_]] {

  def createPool(poolName: String, cores: Int, memory: Int): F[Unit]

}

class CDHYarnClient[F[_] : Sync](http: HttpClient[F],
                                 clusterConfig: ClusterConfig,
                                 clusterService: ClusterService[F])
    extends YarnClient[F]
    with Http4sClientDsl[F]
    with LazyLogging {

  type Memory = Int
  type Cores = Int

  lazy val configURL: F[String] =
    for {
      list <- clusterService.list
      activeCluster <- Sync[F].delay(list.find(_.id == clusterConfig.name).get)
      result <- Sync[F].delay(clusterConfig.serviceConfigUrl(activeCluster.services.find(_.name == "yarn").get.id))
    } yield result

  def config(poolName: String, cores: Cores, memory: Memory): Json = Json.obj(
    "name" -> Json.fromString(poolName.split("\\.").last),
    "schedulablePropertiesList" -> Json.arr(
      Json.obj(
        "maxResources" -> Json.obj(
          "memory" -> Json.fromDouble(memory * 1024).get,
          "vcores" -> Json.fromInt(cores)
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
            case ("name", json) => 
              parent == json.asString.get
            case _ => false
          }.nonEmpty)
        dig(newCursor, newQueue)
    }.getOrElse(cursor)
  }

  def combine(existing: Json, poolName: String, cores: Cores, memory: Memory, parents: Queue[String]): Json =
    dig(existing.hcursor, parents)
      .downField("queues")
      .withFocus(_.withArray(arr => Json.arr(arr :+ config(poolName, cores, memory): _*)))
      .top.get

  def prepare(container: Json, newConfig: Json): Json = {
    import io.circe.optics.JsonPath._
    container.hcursor
      .downField("items")
      .withFocus {
        _.mapArray { arr =>
          arr.map {
            case json if root.name.string.getOption(json).get.contains("yarn_fs_scheduled_allocations") =>
              root.value.string.modify(_ => newConfig.toString())(json)
            case json =>
              json
          }
        }
      }.top.get
  }

  def yarnConfig: F[Json] =
    for {
      url <- configURL
      response <- http.request[Json](Request[F](Method.GET, Uri.unsafeFromString(url)))
    } yield response

  def yarnConfigUpdate(updatedJson: Json): F[Json] =
    for {
      url <- configURL
      _ <- Sync[F].pure(logger.debug("updating yarn at {}", url))
      request <- PUT(Uri.fromString(url).right.get, updatedJson)
      response <- http.request[Json](request)
    } yield response

  def yarnConfigRefresh: F[Json] =
    for {
      _ <- Sync[F].pure(logger.info("refreshing the resource pools via {}", clusterConfig.refreshUrl))
      response <- http.request[Json](Request[F](Method.POST, Uri.fromString(clusterConfig.refreshUrl).right.get))
    } yield response

  def poolConfiguration(config: Json)
                       (implicit evidence: Sync[F]): F[Json] =
    evidence.delay {
      config.hcursor
        .downField("items")
        .downAt {
          item =>
            item.asObject.get.filter { field =>
              field._1 == "name" && field._2.asString.contains("yarn_fs_scheduled_allocations")
            }.nonEmpty
        }.focus.get
    }

  def getParents(poolName: String): F[Queue[String]] = Sync[F].delay {
    logger.debug("getting parents for {}", poolName)
    val fullList = poolName.split("\\.").toList
    logger.debug("{} broken down: {}", poolName, fullList)
    val parents = fullList.filterNot(_ == fullList.last)
    logger.debug("{} parents: {}", poolName, parents)
    Queue(parents: _*)
  }

  override def createPool(poolName: String, cores: Int, memory: Int): F[Unit] =
    for {
      parentPools <- getParents(poolName)
      config <- yarnConfig
      poolConfig <- poolConfiguration(config)
      configJson <- Sync[F].delay(parse((poolConfig \\ "value").head.asString.get).right)
      updatedJson <- Sync[F].delay(combine(configJson.get, poolName, cores, memory, parentPools))
      preparedJson <- Sync[F].delay(prepare(config, updatedJson))
      _ <- yarnConfigUpdate(preparedJson)
      _ <- yarnConfigRefresh
    } yield ()
}
