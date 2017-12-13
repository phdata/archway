package com.heimdali.actors

import java.io.ByteArrayInputStream
import javax.inject.Inject

import akka.actor.Actor
import akka.pattern.pipe
import com.heimdali.actors.WorkspaceSaver.ProjectUpdate
import com.heimdali.models.ViewModel._
import com.heimdali.services.{HDFSClient, KeytabService}
import com.typesafe.config.Config
import org.apache.hadoop.fs.Path

import scala.concurrent.ExecutionContext

object KeytabActor {

  case class GenerateKeytab(id: Long, principal: String)

  case class KeytabCreated(id: Long, hdfsLocation: Path) extends ProjectUpdate {
    override def updateProject(project: SharedWorkspace): SharedWorkspace =
      project.copy(keytabLocation = Some(hdfsLocation.toUri.toString))
  }

}

class KeytabActor @Inject()(hdfsClient: HDFSClient,
                            keytabService: KeytabService,
                            configuration: Config)
                           (implicit val executionContext: ExecutionContext) extends Actor {

  import KeytabActor._

  val basePath: String = configuration.getString("hdfs.project_root")
  def projectPath(principal: String): Path = new Path(basePath, s"$principal/$principal.keytab")

  override val receive: Receive = {
    case GenerateKeytab(id, principal) =>
      (for (
        keytab <- keytabService.generateKeytab(principal);
        filePath <- hdfsClient.uploadFile(new ByteArrayInputStream(keytab.getBytes), projectPath(principal))
      ) yield KeytabCreated(id, filePath)).pipeTo(sender())
  }
}