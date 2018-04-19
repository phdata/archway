package com.heimdali.modules

import com.heimdali.rest.RestAPI
import com.heimdali.startup.Startup

trait AppModule {

//  def startup: Startup

  def restAPI: RestAPI

}
