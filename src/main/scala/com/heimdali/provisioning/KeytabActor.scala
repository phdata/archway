package com.heimdali.provisioning

import java.io.ByteArrayInputStream

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import com.heimdali.clients.HDFSClient
import com.heimdali.services.KeytabService
import com.typesafe.config.Config
import org.apache.hadoop.fs.Path

import scala.concurrent.ExecutionContext

object KeytabActor {
  def props(hdfsClient: HDFSClient, keytabService: KeytabService, configuration: Config)
           (implicit executionContext: ExecutionContext): Props =
    Props(classOf[KeytabActor], hdfsClient, keytabService, configuration, executionContext)


  case class GenerateKeytab(principal: String)

  case class KeytabCreated(hdfsLocation: Path)

}

class KeytabActor(hdfsClient: HDFSClient,
                  keytabService: KeytabService,
                  configuration: Config)
                 (implicit val executionContext: ExecutionContext) extends Actor {

  import KeytabActor._

  val basePath: String = configuration.getString("workspaces.sharedWorkspace.root")

  def projectPath(principal: String): Path = new Path(basePath, s"$principal/$principal.keytab")

  override val receive: Receive = {
    case GenerateKeytab(principal) =>
      (for (
        keytab <- keytabService.generateKeytab(principal);
        filePath <- hdfsClient.uploadFile(new ByteArrayInputStream(keytab.getBytes), projectPath(principal))
      ) yield KeytabCreated(filePath)).pipeTo(sender())
  }
}