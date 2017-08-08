package com.heimdali.services

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class PassiveAccountService @Inject()(implicit val executionContext: ExecutionContext)
  extends AccountService {

  val user = User("Dude Doe", "username", HeimdaliRole.BasicUser)

  override def login(username: String, password: String) = Future {
    Some(user)
  }

  override def refresh(user: User) = Future {
    Token("", "")
  }

  override def validate(token: String) = Future {
    Some(user)
  }
}
