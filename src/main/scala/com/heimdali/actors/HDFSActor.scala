package com.heimdali.actors

import javax.inject.Inject

import akka.actor.Actor
import akka.pattern.pipe
import com.heimdali.actors.ProjectSaver.ProjectUpdate
import com.heimdali.Model._
import com.heimdali.services.{HDFSClient, LoginContextProvider}
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

object HDFSActor {

  case class CreateDirectory(id: Long, name: String, requestedSizeInGB: Double)

  case class HDFSUpdate(id: Long, directory: String) extends ProjectUpdate {
    override def updateProject(project: Project): Project =
      project.copy(hdfs = project.hdfs.copy(location = Some(directory)))
  }

}

class HDFSActor @Inject()(hdfsClient: HDFSClient,
                          configuration: Config,
                          loginContextProvider: LoginContextProvider)
                         (implicit val executionContext: ExecutionContext) extends Actor {

  import HDFSActor._

  val hdfsConfig: Config = configuration.getConfig("hdfs")
  val projectRoot: String = hdfsConfig.getString("project_root")

  def location(name: String) = s"$projectRoot/$name"

  override def receive: Receive = {
    case CreateDirectory(id, name, size) =>
      loginContextProvider.elevate {
        (for (
          path <- hdfsClient.createDirectory(location(name));
          _ <- hdfsClient.setQuota(path, size)
        ) yield HDFSUpdate(id, path.toUri.getPath)).pipeTo(sender())
      }
  }
}
