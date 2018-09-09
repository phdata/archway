package com.heimdali.services

import cats.effect._
import cats.implicits._
import com.heimdali.clients.HttpClient
import com.heimdali.config.ClusterConfig
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.http4s._

class CDHClusterService[F[_]](http: HttpClient[F],
                              clusterConfig: ClusterConfig,
                              hadoopConfiguration: Configuration)
                             (implicit val F: Effect[F])
  extends ClusterService[F] {

  val yarnConfiguration: YarnConfiguration = new YarnConfiguration(hadoopConfiguration)

  import com.heimdali.services.CDHResponses._
  import io.circe.generic.auto._

  def hostListRequest: F[ListContainer[HostInfo]] =
    http.request[ListContainer[HostInfo]](Request(Method.GET, Uri.fromString(clusterConfig.hostListUrl).right.get))

  def clusterDetailsRequest: F[ClusterInfo] =
    http.request[ClusterInfo](Request(Method.GET, Uri.fromString(clusterConfig.clusterUrl).right.get))

  def serviceRoleListRequest(service: String): F[ListContainer[AppRole]] =
    http.request[ListContainer[AppRole]](Request(Method.GET, Uri.fromString(clusterConfig.serviceRoleListUrl(service)).right.get))

  def hostRequest(id: String): F[HostInfo] =
    http.request[HostInfo](Request(Method.GET, Uri.fromString(clusterConfig.hostUrl(id)).right.get))

  def servicesRequest: F[Services] =
    http.request[Services](Request(Method.GET, Uri.fromString(clusterConfig.serviceListUrl).right.get))

  val clusterDetails: F[Cluster] =
    for {
      details <- clusterDetailsRequest
      services <- servicesRequest
      hosts <- hostListRequest

      impala = services.items.find(_.`type` == CDHClusterService.IMPALA_SERVICE_TYPE).get
      impalaDaemonRoles <- serviceRoleListRequest(impala.name).map(_.items.filter(_.`type` == CDHClusterService.ImpalaDaemonRole))

      hive = services.items.find(_.`type` == CDHClusterService.HIVE_SERVICE_TYPE).get
      hiveServer2Roles <- serviceRoleListRequest(hive.name).map(_.items.filter(_.`type` == CDHClusterService.HiveServer2Role))

      hue = services.items.find(_.`type` == CDHClusterService.HUE_SERVICE_TYPE).get
      hueLBRole <- serviceRoleListRequest(hue.name).map(_.items.filter(_.`type` == CDHClusterService.HueLoadBalancerRole))

      yarn = services.items.find(_.`type` == CDHClusterService.YARN_SERVICE_TYPE).get
      yarnRoles <- serviceRoleListRequest(yarn.name)

      nodeManagerRoles = yarnRoles.items.filter(_.`type` == CDHClusterService.NodeManagerRole)
      resourceManagerRoles = yarnRoles.items.filter(_.`type` == CDHClusterService.ResourceManagerRole)
    } yield Cluster(
      details.name,
      details.displayName,
      clusterConfig.url,
      List(
        ClusterApp("impala", impala, hosts, Map(
            "beeswax" -> (21000, impalaDaemonRoles),
            "hiveServer2" -> (21050, impalaDaemonRoles))),
        ClusterApp("hive", hive, hosts, Map("thrift" -> (10000, hiveServer2Roles))),
        ClusterApp("hue", hue, hosts, Map("load_balancer" -> (8888, hueLBRole))),
        ClusterApp("yarn", yarn, hosts, Map(
          "node_manager" -> (8042, nodeManagerRoles),
          "resource_manager" -> (8088, resourceManagerRoles))),
      ),
      CDH(details.fullVersion),
      details.status
    )

  override def list: F[Seq[Cluster]] =
    for {
      details <- clusterDetails
    } yield Seq(details)

}

object CDHClusterService {
  val IMPALA_SERVICE_TYPE = "IMPALA"
  val HIVE_SERVICE_TYPE = "HIVE"
  val HUE_SERVICE_TYPE = "HUE"
  val YARN_SERVICE_TYPE = "YARN"

  val ImpalaDaemonRole: String = "IMPALAD"
  val HiveServer2Role: String = "HIVESERVER2"
  val HueLoadBalancerRole: String = "HUE_LOAD_BALANCER"
  val ResourceManagerRole: String = "RESOURCEMANAGER"
  val NodeManagerRole: String = "NODEMANAGER"
}