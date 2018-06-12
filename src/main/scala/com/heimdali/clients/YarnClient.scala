package com.heimdali.clients

import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.heimdali.config.ClusterConfig
import com.heimdali.models.Yarn
import com.heimdali.services.ClusterService
import com.typesafe.scalalogging.LazyLogging
import io.circe.parser._
import io.circe.{ACursor, Json}
import org.http4s._
import org.http4s.circe._

import scala.collection.immutable.Queue

case class YarnPool(name: String, maxCores: Int, maxMemoryInGB: Double)

trait YarnClient[F[_]] {
  def createPool(yarn: Yarn, parentPools: Queue[String]): F[Unit]
}

class CDHYarnClient[F[_] : Sync](http: HttpClient[F],
                                 clusterConfig: ClusterConfig,
                                 clusterService: ClusterService[F])
  extends YarnClient[F]
    with LazyLogging {

  lazy val configURL: F[String] =
    for {
      list <- clusterService.list
      activeCluster <- Sync[F].delay(list.find(_.id == clusterConfig.name).get)
      result <- Sync[F].delay(clusterConfig.serviceConfigUrl(activeCluster.clusterApps("YARN").id))
    } yield result

  def config(pool: Yarn): Json = Json.obj(
    "name" -> Json.fromString(pool.poolName),
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

  def combine(existing: Json, pool: Yarn, parents: Queue[String]): Json = {
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

  def yarnConfig: F[Json] =
    for {
      url <- configURL
      response <- http.request[Json](Request[F](Method.GET, Uri.fromString(url).toOption.get))
    } yield response

  def yarnConfigUpdate(updatedJson: Json): F[Json] =
    for {
      url <- configURL
      request <- Request[F](Method.PUT, Uri.fromString(url).toOption.get).withBody(updatedJson)
      response <- http.request[Json](request)
    } yield response

  def yarnConfigRefresh: F[Json] =
    for {
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

  override def createPool(yarn: Yarn, parentPools: Queue[String]): F[Unit] =
    for {
      config <- yarnConfig
      poolConfig <- poolConfiguration(config)
      configJson <- Sync[F].delay(parse((poolConfig \\ "value").head.asString.get).right)
      updatedJson <- Sync[F].delay(combine(configJson.get, yarn, parentPools))
      preparedJson <- Sync[F].delay(prepare(config, updatedJson))
      _ <- yarnConfigUpdate(preparedJson)
      _ <- yarnConfigRefresh
    } yield ()
}
