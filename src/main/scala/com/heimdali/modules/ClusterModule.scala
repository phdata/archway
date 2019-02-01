package com.heimdali.modules

import cats.effect.Effect
import com.heimdali.services.{CDHClusterService, ClusterService}

trait ClusterModule[F[_]] {
  this: ConfigurationModule
    with ServiceModule[F]
    with HttpModule[F] =>

  implicit def effect: Effect[F]

  val clusterService: ClusterService[F] = new CDHClusterService[F](http, appConfig.cluster, hadoopConfiguration, fileReader, hueConfigurationReader)

}
