package com.heimdali.services

import cats.effect._
import cats.implicits._
import com.heimdali.clients.HttpClient
import com.heimdali.config.ClusterConfig
import org.http4s._

class CDHClusterService[F[_] : Effect](http: HttpClient[F],
                                       clusterConfig: ClusterConfig)
  extends ClusterService[F] {

  import com.heimdali.services.CDHResponses._
  import io.circe.generic.auto._

  def clusterDetailsRequest: F[ClusterInfo] =
    for {
      response <- http.request[ClusterInfo](Request(Method.GET, Uri.fromString(clusterConfig.clusterUrl).right.get))
    } yield response

  def serviceRoleListRequest(service: String): F[ImpalaApp] =
    for {
      response <- http.request[ImpalaApp](Request(Method.GET, Uri.fromString(clusterConfig.serviceRoleListUrl(service)).right.get))
    } yield response

  def hostRequest(id: String): F[HostInfo] =
    for {
      response <- http.request[HostInfo](Request(Method.GET, Uri.fromString(clusterConfig.hostUrl(id)).right.get))
    } yield response

  def servicesRequest: F[Services] =
    for {
      response <- http.request[Services](Request(Method.GET, Uri.fromString(clusterConfig.serviceListUrl).right.get))
    } yield response

  val clusterDetails: F[Cluster] =
    for (
      details <- clusterDetailsRequest;
      services <- servicesRequest;
      impala <- serviceRoleListRequest(services.items.find(_.`type` == CDHClusterService.IMPALA_SERVICE_TYPE).get.name);
      hive <- serviceRoleListRequest(services.items.find(_.`type` == CDHClusterService.HIVE_SERVICE_TYPE).get.name);
      impalaHost <- hostRequest(impala.hostname(CDHClusterService.ImpalaDaemonRole).get);
      hiveHost <- hostRequest(hive.hostname(CDHClusterService.HiveServer2Role).get)
    ) yield Cluster(
      details.name,
      details.displayName,
      services.items.map {
        case ServiceInfo(id, CDHClusterService.IMPALA_SERVICE_TYPE, state, status, display) =>
          CDHClusterService.IMPALA_SERVICE_TYPE -> HostClusterApp(id, display, status, state, impalaHost.hostname)
        case ServiceInfo(id, CDHClusterService.HIVE_SERVICE_TYPE, state, status, display) =>
          CDHClusterService.HiveServer2Role -> HostClusterApp(id, display, status, state, hiveHost.hostname)
        case ServiceInfo(id, serviceType, state, status, display) =>
          serviceType -> BasicClusterApp(id, display, status, state)
      }.toMap,
      CDH(details.fullVersion),
      details.status
    )

  override def list: F[Seq[Cluster]] =
    for {
      details <- clusterDetails
    } yield Seq(details)

}

object CDHClusterService {
  val ImpalaDaemonRole: String = "IMPALAD"
  val IMPALA_SERVICE_TYPE = "IMPALA"
  val HiveServer2Role: String = "HIVESERVER2"
  val HIVE_SERVICE_TYPE = "HIVE"
}