package com.heimdali.services

import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.heimdali.clients.HttpClient
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Decoder
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

object CDHResponses {

  case class ClusterInfo(name: String, displayName: String, fullVersion: String, status: String)

  case class HostRef(hostId: String)

  case class AppRole(name: String, roleName: String, hostRef: HostRef)

  case class ImpalaApp(items: Seq[AppRole]) {
    def hostname(name: String): Option[String] =
      items.find(_.roleName == name).map(_.hostRef.hostId)
  }

  case class HiveApp(items: Seq[AppRole]) {
    def hostname(name: String): Option[String] =
      items.find(_.roleName == name).map(_.hostRef.hostId)
  }

  case class Services(items: Seq[ServiceInfo])

  case class ServiceInfo(name: String, `type`: String, serviceState: String, entityStatus: String, displayName: String)

  case class HostInfo(hostId: String, hostname: String)

  implicit val decodeServiceInfo: Decoder[ServiceInfo] =
    Decoder.forProduct5("name", "type", "serviceState", "entityStatus", "displayName")(ServiceInfo.apply)

  implicit val decodeImpalaItem: Decoder[AppRole] =
    Decoder.forProduct3("name", "type", "hostRef")(AppRole.apply)

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

  def impalaRequest(service: String): Future[ImpalaApp] =
    http.request(secureRequest(Get(s"$baseUrl/clusters/$cluster/services/$service/roles")))
      .flatMap(extract[ImpalaApp])

  def hiveRequest(service: String): Future[HiveApp] =
    http.request(secureRequest(Get(s"$baseUrl/clusters/$cluster/services/$service/roles")))
      .flatMap(extract[HiveApp])

  def hostRequest(id: String): Future[HostInfo] =
    http.request(secureRequest(Get(s"$baseUrl/hosts/$id")))
      .flatMap(extract[HostInfo])

  def servicesRequest: Future[Services] =
    http.request(secureRequest(Get(s"$baseUrl/clusters/$cluster/services")))
      .flatMap(extract[Services])

  def clusterDetails(url: String, username: String, password: String): Future[Cluster] =
    for (
      details <- clusterDetailsRequest;
      services <- servicesRequest;
      impala <- impalaRequest(services.items.find(_.`type` == CDHClusterService.IMPALA_SERVICE_TYPE).get.name);
      hive <- hiveRequest(services.items.find(_.`type` == CDHClusterService.HIVE_SERVICE_TYPE).get.name);
      impalaHost <- hostRequest(impala.hostname(CDHClusterService.ImpalaDaemonRole).get);
      hiveHost <- hostRequest(hive.hostname(CDHClusterService.HiveServer2Role).get)
    ) yield Cluster(
      details.name,
      details.displayName,
      services.items.map {
        case ServiceInfo(_, CDHClusterService.IMPALA_SERVICE_TYPE, state, status, display) =>
          CDHClusterService.IMPALA_SERVICE_TYPE -> HostClusterApp(display, status, state, impalaHost.hostname)
        case ServiceInfo(_, CDHClusterService.HIVE_SERVICE_TYPE, state, status, display) =>
          CDHClusterService.HiveServer2Role -> HostClusterApp(display, status, state, hiveHost.hostname)
        case ServiceInfo(_, serviceType, state, status, display) =>
          serviceType -> BasicClusterApp(display, status, state)
      }.toMap,
      CDH(details.fullVersion),
      details.status
    )

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
  val IMPALA_SERVICE_TYPE = "IMPALA"
  val HiveServer2Role: String = "HIVESERVER2"
  val HIVE_SERVICE_TYPE = "HIVE"
}
