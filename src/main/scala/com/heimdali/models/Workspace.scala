package com.heimdali.models

import com.typesafe.config.Config

trait Workspace {
  def workspaceId: String

  def databaseName: String

  def role: String

  def dataDirectory(configuration: Config): String

  def groupName: String

  def initialMembers: Seq[String]

  def onBehalfOf: Option[String]
}
