package com.heimdali.modules

import com.heimdali.services.{LoginContextProvider, UGILoginContextProvider}

trait ContextModule[F[_]] {
  this: AppModule[F]
    with ExecutionContextModule
    with ConfigurationModule =>

  val loginContextProvider: LoginContextProvider = new UGILoginContextProvider()
}
