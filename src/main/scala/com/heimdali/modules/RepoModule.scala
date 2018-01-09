package com.heimdali.modules

import com.heimdali.repositories.{WorkspaceRepository, WorkspaceRepositoryImpl}

trait RepoModule {
  this: ExecutionContextModule =>

  val workspaceRepository: WorkspaceRepository = new WorkspaceRepositoryImpl

}
