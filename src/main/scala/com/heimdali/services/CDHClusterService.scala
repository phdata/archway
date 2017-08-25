package com.heimdali.services

import javax.inject.Inject

import play.api.Configuration
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, _}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class CDHClusterService @Inject()(wsClient: WSClient, configuration: Configuration)
                                 (implicit val executionContext: ExecutionContext) extends ClusterService {

  def clusterReads(id: String): Reads[Cluster] = (
    Reads.pure(id) ~
      (__ \ "displayName").read[String] ~
      (__ \ "fullVersion").read[String].map(CDH)
    ) (Cluster)

  override def list: Future[Seq[Cluster]] = {
    val clusters = configuration.get[Configuration]("clusters")
    Future.sequence {
      clusters.keys.map { cluster =>
        val clusterConfig = clusters.get[Configuration](cluster)
        val id = clusterConfig.get[String]("id")
        wsClient.url(s"${clusterConfig.get[String]("base-url")}/clusters/$cluster")
          .get()
          .map(_.json.as[Cluster](clusterReads(id)))
      }.toSeq
    }
  }

}
