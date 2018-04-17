package com.heimdali.modules

import com.heimdali.services.{LoginContextProvider, UGILoginContextProvider}

trait ContextModule {
  this: ConfigurationModule with ExecutionContextModule =>

  val loginContextProvider: LoginContextProvider = new UGILoginContextProvider()
}
