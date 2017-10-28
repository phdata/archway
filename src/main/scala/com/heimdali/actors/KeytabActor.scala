package com.heimdali.actors

import java.io.ByteArrayInputStream
import javax.inject.Inject

import akka.actor.Actor
import akka.pattern.pipe
import com.heimdali.actors.ProjectSaver.ProjectUpdate
import com.heimdali.models.Project
import com.heimdali.services.{HDFSClient, KeytabService}
import org.apache.hadoop.fs.Path
import play.api.Configuration

import scala.concurrent.ExecutionContext

object KeytabActor {

  case class GenerateKeytab(id: Long, principal: String)

  case class KeytabCreated(id: Long, hdfsLocation: Path) extends ProjectUpdate {
    override def updateProject(project: Project): Project =
      project.copy(keytabLocation = Some(hdfsLocation.toUri.toString))
  }

}

class KeytabActor @Inject()(hdfsClient: HDFSClient,
                            keytabService: KeytabService,
                            configuration: Configuration)
                           (implicit val executionContext: ExecutionContext) extends Actor {

  import KeytabActor._

  val basePath: String = configuration.get[String]("hdfs.project_root")
  def projectPath(principal: String): Path = new Path(basePath, s"$principal/$principal.keytab")

  override val receive: Receive = {
    case GenerateKeytab(id, principal) =>
      (for (
        keytab <- keytabService.generateKeytab(principal);
        filePath <- hdfsClient.uploadFile(new ByteArrayInputStream(keytab.getBytes), projectPath(principal))
      ) yield KeytabCreated(id, filePath)).pipeTo(sender())
  }
}