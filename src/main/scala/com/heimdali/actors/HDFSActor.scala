package com.heimdali.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.heimdali.services.{HDFSClient, LoginContextProvider}
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

object HDFSActor {

  case class CreateSharedDirectory(name: String, requestedSizeInGB: Double)

  case class CreateUserDirectory(username: String)

  case class DirectoryCreated(path: String)

}

class HDFSActor(hdfsClient: HDFSClient,
                loginContextProvider: LoginContextProvider,
                configuration: Config)
               (implicit val executionContext: ExecutionContext) extends Actor {

  import HDFSActor._

  val hdfsConfig: Config = configuration.getConfig("hdfs")
  val projectRoot: String = hdfsConfig.getString("projectRoot")
  val usertRoot: String = hdfsConfig.getString("userRoot")

  override def receive: Receive = {
    case CreateUserDirectory(username) =>
      val path: String = s"$usertRoot/$username/db"
      hdfsClient.createDirectory(path, Some(username))
        .map(_ => DirectoryCreated(path))
        .pipeTo(sender())

    case CreateSharedDirectory(name, size) =>
      val path: String = s"$projectRoot/$name"
      hdfsClient
        .createDirectory(path, None)
        .map(_ => DirectoryCreated(path))
        .pipeTo(sender())
  }
}