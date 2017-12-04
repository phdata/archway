package com.heimdali.startup

import javax.security.auth.Subject

import com.heimdali.services.LoginContextProvider
import org.apache.hadoop.security.UserGroupInformation

class UGISecurityContext (loginContextProvider: LoginContextProvider)
  extends SecurityContext {

  override def login(username: String, password: String): Subject = {
    val subject = loginContextProvider.login(username, password)
    UserGroupInformation.loginUserFromSubject(subject)
    subject
  }

}
