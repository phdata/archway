package com.heimdali.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.heimdali.actors.WorkspaceSaver.ProjectUpdate
import com.heimdali.models.ViewModel._
import com.heimdali.services.HDFSClient
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

object HDFSActor {

  case class CreateDirectory(id: Long, name: String, requestedSizeInGB: Double)

  case class HDFSUpdate(id: Long, directory: String, actualGB: Double) extends ProjectUpdate {
    override def updateProject(project: SharedWorkspace): SharedWorkspace =
      project.copy(hdfs = project.hdfs.copy(location = Some(directory), actualGB = Some(actualGB)))
  }

}

class HDFSActor(hdfsClient: HDFSClient,
                configuration: Config)
               (implicit val executionContext: ExecutionContext) extends Actor {

  import HDFSActor._

  val hdfsConfig: Config = configuration.getConfig("hdfs")
  val projectRoot: String = hdfsConfig.getString("project_root")

  def location(name: String) = s"$projectRoot/$name"

  override def receive: Receive = {
    case CreateDirectory(id, name, size) =>
      (for (
        path <- hdfsClient.createDirectory(location(name))
      ) yield HDFSUpdate(id, path.toUri.getPath, size)).pipeTo(sender())
  }
}
