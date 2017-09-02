package com.heimdali.services

import javax.inject.Inject

import play.api.Configuration
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, _}
import play.api.libs.ws.{WSAuthScheme, WSClient}

import scala.concurrent.{ExecutionContext, Future}

class CDHClusterService @Inject()(wsClient: WSClient, configuration: Configuration)
                                 (implicit val executionContext: ExecutionContext) extends ClusterService {

  implicit val clusterReads: Reads[Cluster] = (
    (__ \ "name").read[String] ~
      (__ \ "displayName").read[String] ~
      (__ \ "fullVersion").read[String].map(CDH)
    ) (Cluster)

  override def list: Future[Seq[Cluster]] = {
    val clusters = configuration.get[Configuration]("clusters")
    Future.sequence {
      clusters.keys.map { cluster =>
        val clusterConfig = clusters.get[Configuration](cluster)
        val baseUrl = clusterConfig.get[String]("url")
        val username = clusterConfig.get[String]("username")
        val password = clusterConfig.get[String]("password")

        wsClient
          .url(s"$baseUrl/clusters/$cluster")
          .withAuth(username, password, WSAuthScheme.BASIC)
          .get()
          .map(_.json.as[Cluster])
      }.toSeq
    }
  }

}
