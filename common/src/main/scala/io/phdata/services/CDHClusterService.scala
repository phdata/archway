package io.phdata.services

import cats.effect._
import cats.implicits._
import io.phdata.caching.Cached
import io.phdata.clients.HttpClient
import io.phdata.config.{ClusterConfig, ServiceOverride}
import com.typesafe.scalalogging.LazyLogging
import io.phdata.services.CDHResponses._
import org.apache.hadoop.conf.Configuration
import org.http4s._
import scala.concurrent.duration._
import io.circe.generic.auto._

class CDHClusterService[F[_]: ConcurrentEffect: Clock](
    http: HttpClient[F],
    clusterConfig: ClusterConfig,
    hadoopConfiguration: Configuration,
    cacheService: CacheService,
    clusterCache: Cached[F, Seq[Cluster]]
) extends ClusterService[F] with LazyLogging {

  def hostListRequest: F[ListContainer[HostInfo]] =
    http.request[ListContainer[HostInfo]](Request(Method.GET, Uri.fromString(clusterConfig.hostListUrl).right.get))

  def clusterDetailsRequest: F[ClusterInfo] =
    http.request[ClusterInfo](Request(Method.GET, Uri.fromString(clusterConfig.clusterUrl).right.get))

  def serviceRoleListRequest(service: String): F[ListContainer[AppRole]] =
    http.request[ListContainer[AppRole]](
      Request(Method.GET, Uri.fromString(clusterConfig.serviceRoleListUrl(service)).right.get)
    )

  def hostRequest(id: String): F[HostInfo] =
    http.request[HostInfo](Request(Method.GET, Uri.fromString(clusterConfig.hostUrl(id)).right.get))

  def servicesRequest: F[Services] =
    http.request[Services](Request(Method.GET, Uri.fromString(clusterConfig.serviceListUrl).right.get))

  def mgmtServicesRequest: F[ServiceInfo] =
    http.request[ServiceInfo](Request(Method.GET, Uri.fromString(clusterConfig.mgmtServiceUrl).right.get))

  def mgmtRoleListRequest: F[ListContainer[AppRole]] =
    http.request[ListContainer[AppRole]](Request(Method.GET, Uri.fromString(clusterConfig.mgmtRoleListUrl).right.get))

  def mgmtRoleConfigGroupsRequest(appRole: Option[AppRole]): F[ListContainer[RoleConfigGroup]] = {
    appRole match {
      case Some(roleCGN) =>
        Uri.fromString(clusterConfig.mgmtRoleConfigGroups(roleCGN.roleConfigGroupRef.roleConfigGroupName)) match {
          case Right(uri) =>
            http.request[ListContainer[RoleConfigGroup]](Request(Method.GET, uri))
          case Left(failure) => {
            logger.error("Uri cannot be parsed {}", failure)
            ListContainer(List.empty[RoleConfigGroup]).pure[F]
          }
        }
      case None => ListContainer(List.empty[RoleConfigGroup]).pure[F]
    }
  }

  def hueApp(hue: ServiceInfo, hosts: ListContainer[HostInfo], hueLBRole: List[AppRole]): ClusterApp =
    clusterConfig.hueOverride match {
      case ServiceOverride(Some(host), port) =>
        ClusterApp("hue", "hue", "NA", "NA", Map("load_balancer" -> List(AppLocation(host, port))))
      case _ =>
        ClusterApp("hue", hue, hosts, Map("load_balancer" -> (clusterConfig.hueOverride.port, hueLBRole)))
    }

  lazy val clusterDetails: F[Seq[Cluster]] =
    for {
      details <- clusterDetailsRequest
      services <- servicesRequest
      hosts <- hostListRequest

      impala = services.items.find(_.`type` == CDHClusterService.IMPALA_SERVICE_TYPE).get
      impalaDaemonRoles <- serviceRoleListRequest(impala.name)
        .map(_.items.filter(_.`type` == CDHClusterService.ImpalaDaemonRole))

      hive = services.items.find(_.`type` == CDHClusterService.HIVE_SERVICE_TYPE).get
      hiveServer2Roles <- serviceRoleListRequest(hive.name)
        .map(_.items.filter(_.`type` == CDHClusterService.HiveServer2Role))

      hue = services.items.find(_.`type` == CDHClusterService.HUE_SERVICE_TYPE).get
      hueLBRole <- serviceRoleListRequest(hue.name)
        .map(_.items.filter(_.`type` == CDHClusterService.HueLoadBalancerRole))

      mgmt <- mgmtServicesRequest
      mgmtNavigatorRole <- mgmtRoleListRequest.map(_.items.filter(_.`type` == CDHClusterService.MgmtNavigatorRole))
      mgmtNavigatorMetaServerRole <- mgmtRoleListRequest.map(
        _.items.filter(_.`type` == CDHClusterService.MgmtNavigatorMetaServerRole)
      )
      mgmtRoleConfigGroups <- mgmtRoleConfigGroupsRequest(mgmtNavigatorMetaServerRole.headOption)

    } yield Cluster(
      details.name,
      details.displayName,
      clusterConfig.url,
      List(
        ClusterApp(
          "impala",
          impala,
          hosts,
          Map(
            "beeswax" -> (clusterConfig.beeswaxPort, impalaDaemonRoles),
            "hiveServer2" -> (clusterConfig.hiveServer2Port, impalaDaemonRoles)
          )
        ),
        ClusterApp(
          "hive",
          hive,
          hosts,
          Map("thrift" -> (hadoopConfiguration.get("hive.server2.thrift.port", "10000").toInt, hiveServer2Roles))
        ),
        hueApp(hue, hosts, hueLBRole),
        ClusterApp(
          "mgmt",
          mgmt,
          hosts,
          Map(
            "navigator" -> (mgmtRoleConfigGroups.items
                  .find(_.name == "navigator_server_port")
                  .map(p => p.value.getOrElse(p.default).toInt)
                  .getOrElse(7187), mgmtNavigatorRole)
          )
        )
      ),
      CDH(details.fullVersion),
      details.status
    ) :: Nil

  private def resolvePort(roleProperties: ListContainer[RoleProperty], defaultPort: Int, name: String): Int = {
    roleProperties.items
      .find(_.relatedName == name)
      .map(property => property.value.getOrElse(property.default.getOrElse(defaultPort.toString)))
      .map(_.toInt)
      .getOrElse(defaultPort)
  }

  override def list: F[Seq[Cluster]] = cacheService.getOrRun[F, Seq[Cluster]](1 hour, clusterDetails, clusterCache)
}

object CDHClusterService {
  val IMPALA_SERVICE_TYPE = "IMPALA"
  val HIVE_SERVICE_TYPE = "HIVE"
  val HUE_SERVICE_TYPE = "HUE"

  val ImpalaDaemonRole: String = "IMPALAD"
  val HiveServer2Role: String = "HIVESERVER2"
  val HueLoadBalancerRole: String = "HUE_LOAD_BALANCER"
  val ResourceManagerRole: String = "RESOURCEMANAGER"
  val NodeManagerRole: String = "NODEMANAGER"
  val MgmtNavigatorRole: String = "NAVIGATOR"
  val MgmtNavigatorMetaServerRole: String = "NAVIGATORMETASERVER"
}
