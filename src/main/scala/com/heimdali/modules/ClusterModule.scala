package com.heimdali.modules

import com.heimdali.services.{CDHClusterService, ClusterService}

trait ClusterModule[F[_]] {
  this: AppModule[F]
    with ConfigurationModule
    with HttpModule[F] =>

  val clusterService: ClusterService[F] = new CDHClusterService[F](http, appConfig.cluster)

}
