package com.heimdali.provisioning

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import com.heimdali.clients.{HDFSAllocation, HDFSClient}
import com.heimdali.services.LoginContextProvider

import scala.concurrent.ExecutionContext

object HDFSActor {

  case class CreateDirectory(path: String, requestedSizeInGB: Int, onBehalfOf: Option[String])

  case class DirectoryCreated(hdfsAllocation: HDFSAllocation)

  def props(hdfsClient: HDFSClient, loginContextProvider: LoginContextProvider)
           (implicit executionContext: ExecutionContext) =
    Props(classOf[HDFSActor], hdfsClient, loginContextProvider, executionContext)

}

class HDFSActor(hdfsClient: HDFSClient,
                loginContextProvider: LoginContextProvider)
               (implicit val executionContext: ExecutionContext) extends Actor {

  import HDFSActor._

  override def receive: Receive = {

    case CreateDirectory(path, size, onBehalfOf) =>
      (for {
        path <- hdfsClient.createDirectory(path, onBehalfOf)
        allocation <- hdfsClient.setQuota(path, size)
      } yield DirectoryCreated(allocation))
        .pipeTo(sender())
  }
}