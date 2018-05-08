package com.heimdali.models

import com.typesafe.config.Config

trait Workspace {
  def workspaceId: String

  def databaseName: String

  def role(configuration: Config): String

  def dataDirectory(configuration: Config): String

  def groupName(configuration: Config): String

  def initialMembers: Seq[String]

  def onBehalfOf: Option[String]
}
