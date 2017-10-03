package com.heimdali.actors

import javax.inject.Inject

import akka.actor.Actor
import akka.pattern.pipe
import com.heimdali.services.HDFSClient
import play.api.Configuration

import scala.concurrent.ExecutionContext

object HDFSActor {

  case class CreateDirectory(name: String, requestedSizeInGB: Double)

  case class HDFSDone(directory: String)

}

class HDFSActor @Inject()(hdfsClient: HDFSClient,
                          configuration: Configuration)
                         (implicit val executionContext: ExecutionContext) extends Actor {

  import HDFSActor._

  val hdfsConfig: Configuration = configuration.get[Configuration]("hdfs")
  val projectRoot: String = hdfsConfig.get[String]("project_root")

  def location(name: String) = s"$projectRoot/$name"

  override def receive: Receive = {
    case CreateDirectory(name, size) =>
      (for (
        path <- hdfsClient.createDirectory(location(name));
        _ <- hdfsClient.setQuota(path, size)
      ) yield HDFSDone(path.toUri.getPath)).pipeTo(sender())
  }
}
