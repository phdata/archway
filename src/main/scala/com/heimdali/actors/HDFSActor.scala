package com.heimdali.actors

import javax.inject.Inject

import akka.actor.Actor
import akka.pattern.pipe
import com.heimdali.actors.ProjectSaver.ProjectUpdate
import com.heimdali.models.Project
import com.heimdali.services.HDFSClient
import play.api.Configuration

import scala.concurrent.ExecutionContext

object HDFSActor {

  case class CreateDirectory(id: Long, name: String, requestedSizeInGB: Double)

  case class HDFSUpdate(id: Long, directory: String) extends ProjectUpdate {
    override def updateProject(project: Project): Project =
      project.copy(hdfs = project.hdfs.copy(location = Some(directory)))
  }

}

class HDFSActor @Inject()(hdfsClient: HDFSClient,
                          configuration: Configuration)
                         (implicit val executionContext: ExecutionContext) extends Actor {

  import HDFSActor._

  val hdfsConfig: Configuration = configuration.get[Configuration]("hdfs")
  val projectRoot: String = hdfsConfig.get[String]("project_root")

  def location(name: String) = s"$projectRoot/$name"

  override def receive: Receive = {
    case CreateDirectory(id, name, size) =>
      (for (
        path <- hdfsClient.createDirectory(location(name));
        _ <- hdfsClient.setQuota(path, size)
      ) yield HDFSUpdate(id, path.toUri.getPath)).pipeTo(sender())
  }
}
