package com.heimdali.startup

import javax.inject.Inject

import com.heimdali.services.LoginContextProvider
import org.apache.hadoop.security.UserGroupInformation

class UGISecurityContext @Inject()(loginContextProvider: LoginContextProvider)
  extends SecurityContext {
  override def login(username: String, password: String) = {
    val subject = loginContextProvider.login(username, password)
    UserGroupInformation.loginUserFromSubject(subject)
    subject
  }
}
