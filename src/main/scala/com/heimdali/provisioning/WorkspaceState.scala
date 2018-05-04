package com.heimdali.provisioning

import akka.persistence.fsm.PersistentFSM.FSMState

private[provisioning] sealed trait WorkspaceState extends FSMState

private[provisioning] case object Idle extends WorkspaceState {
  override def identifier = "Waiting to provision"
}

private[provisioning] case object Provisioning extends WorkspaceState {
  override def identifier = "Provisioning shared workspace"
}

private[provisioning] case object Saving extends WorkspaceState {
  override def identifier = "Saving shared workspace"
}