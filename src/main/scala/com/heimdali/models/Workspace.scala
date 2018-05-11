package com.heimdali.models

import com.typesafe.config.Config

import scala.collection.immutable.Queue

import scala.collection.JavaConverters._

trait Workspace[T] {
  def configName: String

  def workspaceId: T

  def poolName: String

  def requestedCores(configuration: Config): Int =
    configuration.getInt(s"workspaces.$configName.defaultCores")

  def requestedMemory(configuration: Config): Int =
    configuration.getInt(s"workspaces.$configName.defaultMemory")

  def requestedDiskSize(configuration: Config): Int =
    configuration.getInt(s"workspaces.$configName.defaultSize")

  def parentPools(configuration: Config): Queue[String] =
    Queue(configuration.getString(s"workspaces.$configName.poolParents").split(",") :_*)

  def databaseName: String

  def role(configuration: Config): String

  def dataDirectory(configuration: Config): String

  def groupName(configuration: Config): String

  def initialMembers: Seq[String]

  def onBehalfOf: Option[String]
}
