package com.heimdali.modules

import com.heimdali.services.{LoginContextProvider, UGILoginContextProvider}

trait ContextModule[F[_]] {
  this: ExecutionContextModule[F]
    with ConfigurationModule =>

  val loginContextProvider: LoginContextProvider = new UGILoginContextProvider()
}
