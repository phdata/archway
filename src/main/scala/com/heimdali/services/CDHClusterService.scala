package com.heimdali.services

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.{Decoder, HCursor}

import scala.concurrent.{ExecutionContext, Future}

class CDHClusterService(http: HttpExt,
                        configuration: Config)
                       (implicit executionContext: ExecutionContext,
                        materializer: Materializer)
  extends ClusterService with FailFastCirceSupport {

  implicit val decoder = new Decoder[Cluster] {
    override def apply(c: HCursor) = for {
      id <- c.downField("name").as[String]
      name <- c.downField("displayName").as[String]
      version <- c.downField("fullVersion").as[String]
    } yield Cluster(id, name, CDH(version))
  }

  def clusterDetails(baseUrl: String, username: String, password: String): Future[Cluster] = {
    val request = Get(baseUrl).addCredentials(BasicHttpCredentials(username, password))
    http.singleRequest(request)
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          Unmarshal(entity).to[Cluster]
      }
  }

  override def list: Future[Seq[Cluster]] = {
    val clusterConfig = configuration.getConfig("cluster")
    val baseUrl = clusterConfig.getString("url")
    val username = clusterConfig.getString("username")
    val password = clusterConfig.getString("password")

    clusterDetails(baseUrl, username, password).map(Seq(_))
  }

}
