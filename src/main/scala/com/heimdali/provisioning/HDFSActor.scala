package com.heimdali.provisioning

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import com.heimdali.clients.HDFSClient
import com.heimdali.services.LoginContextProvider
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

object HDFSActor {

  case class CreateDirectory(path: String, requestedSizeInGB: Int, onBehalfOf: Option[String])

  case class DirectoryCreated(path: String)

  def props(hdfsClient: HDFSClient, loginContextProvider: LoginContextProvider, configuration: Config)
           (implicit executionContext: ExecutionContext) =
    Props(classOf[HDFSActor], hdfsClient, loginContextProvider, configuration, executionContext)

}

class HDFSActor(hdfsClient: HDFSClient,
                loginContextProvider: LoginContextProvider,
                configuration: Config)
               (implicit val executionContext: ExecutionContext) extends Actor {

  import HDFSActor._

  override def receive: Receive = {

    case CreateDirectory(path, size, onBehalfOf) =>
      hdfsClient
        .createDirectory(path, onBehalfOf)
        .map(_ => DirectoryCreated(path))
        .pipeTo(sender())
  }
}