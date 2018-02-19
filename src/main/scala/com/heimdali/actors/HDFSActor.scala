package com.heimdali.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.heimdali.actors.WorkspaceSaver.ProjectUpdate
import com.heimdali.models.ViewModel._
import com.heimdali.services.HDFSClient
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

object HDFSActor {

  case class CreateSharedDirectory(name: String, requestedSizeInGB: Double)

  case class CreateUserDirectory(username: String)

  case class DirectoryCreated(path: String)

}

class HDFSActor(hdfsClient: HDFSClient,
                configuration: Config)
               (implicit val executionContext: ExecutionContext) extends Actor {

  import HDFSActor._

  val hdfsConfig: Config = configuration.getConfig("hdfs")
  val projectRoot: String = hdfsConfig.getString("projectRoot")
  val usertRoot: String = hdfsConfig.getString("userRoot")

  override def receive: Receive = {
    case CreateUserDirectory(username) =>
      val path: String = s"$usertRoot/$username/db"
      (for (
        _ <- hdfsClient.createDirectory(path);
        _ <- hdfsClient.changeOwner(path, username)
      ) yield DirectoryCreated(path)).pipeTo(sender())

    case CreateSharedDirectory(name, size) =>
      val path: String = s"$projectRoot/$name"
      (for (
        _ <- hdfsClient.createDirectory(path);
        _ <- hdfsClient.changeOwner(path, "hive")
      ) yield DirectoryCreated(path)).pipeTo(sender())
  }
}
