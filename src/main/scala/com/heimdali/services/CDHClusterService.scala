package com.heimdali.services

import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Decoder
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

object CDHResponses {

  case class ClusterInfo(name: String, displayName: String, fullVersion: String, status: String)

  case class HostRef(hostId: String)

  case class ImpalaAppItem(name: String, roleName: String, hostRef: HostRef)

  case class ImpalaApp(items: Seq[ImpalaAppItem]) {
    def hostname(name: String): Option[String] =
      items.find(_.roleName == name).map(_.hostRef.hostId)
  }

  case class HostInfo(hostId: String, hostname: String)

  implicit val decodeImpalaItem: Decoder[ImpalaAppItem] =
    Decoder.forProduct3("name", "type", "hostRef")(ImpalaAppItem.apply)

  implicit val decodeClusterInfo: Decoder[ClusterInfo] =
    Decoder.forProduct4("name", "displayName", "fullVersion", "entityStatus")(ClusterInfo.apply)
}

class CDHClusterService(http: HttpClient,
                        configuration: Config)
                       (implicit executionContext: ExecutionContext,
                        materializer: Materializer)
  extends ClusterService with FailFastCirceSupport {

  import CDHResponses._

  val clusterConfig: Config = configuration.getConfig("cluster")
  val baseUrl: String = clusterConfig.getString("url")
  val cluster: String = clusterConfig.getString("name")
  val adminConfig = clusterConfig.getConfig("admin")
  val username: String = adminConfig.getString("username")
  val password: String = adminConfig.getString("password")

  def secureRequest(httpRequest: HttpRequest): HttpRequest =
    httpRequest.addCredentials(BasicHttpCredentials(username, password))

  def clusterDetailsRequest: Future[ClusterInfo] =
    http.request(secureRequest(Get(s"$baseUrl/clusters/$cluster")))
      .flatMap(extract[ClusterInfo])

  def impalaRequest: Future[ImpalaApp] =
    http.request(secureRequest(Get(s"$baseUrl/clusters/$cluster/services/impala/roles")))
      .flatMap(extract[ImpalaApp])

  def hostRequest(id: String): Future[HostInfo] =
    http.request(secureRequest(Get(s"$baseUrl/hosts/$id")))
      .flatMap(extract[HostInfo])

  def clusterDetails(url: String, username: String, password: String): Future[Cluster] =
    for (
      details <- clusterDetailsRequest;
      impala <- impalaRequest;
      host <- hostRequest(impala.hostname(CDHClusterService.ImpalaDaemonRole).get)
    ) yield Cluster(details.name, details.displayName, ClusterApps(Impala(host.hostname)), CDH(details.fullVersion), details.status)

  def extract[A](httpResponse: HttpResponse)(implicit decoder: Decoder[A]): Future[A] =
    httpResponse match {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        Unmarshal(entity).to[A]
    }

  override def list: Future[Seq[Cluster]] =
    clusterDetails(s"$baseUrl/clusters/$cluster", username, password).map(Seq(_))

}

object CDHClusterService {
  val ImpalaDaemonRole: String = "IMPALAD"
}