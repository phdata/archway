package com.heimdali.test.fixtures

import javax.inject.Inject

import akka.stream.Materializer
import com.heimdali.services.{CDH, CDHClusterService, Cluster}
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}

class FakeClusterService @Inject()(configuration: Config)
                                  (implicit executionContext: ExecutionContext,
                                   materializer: Materializer)
  extends CDHClusterService(null, configuration) {
  override def clusterDetails(baseUrl: String, username: String, password: String) =
    Future {
      Cluster(username, username, FakeClusterService.cdh)
    }
}

object FakeClusterService {
  val cdh = CDH("1.0")
}