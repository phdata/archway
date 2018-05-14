package com.heimdali.modules

import com.heimdali.services.{CDHClusterService, ClusterService}

trait ClusterModule {
  this: ConfigurationModule
    with HttpModule
    with ExecutionContextModule =>

  val clusterService: ClusterService = new CDHClusterService(http, configuration)

}
