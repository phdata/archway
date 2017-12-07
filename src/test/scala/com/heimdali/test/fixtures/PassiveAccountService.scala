package com.heimdali.test.fixtures

import javax.inject.Inject

import com.heimdali.services.{AccountService, Token, User}

import scala.concurrent.{ExecutionContext, Future}

class PassiveAccountService @Inject()(implicit val executionContext: ExecutionContext)
  extends AccountService {

  val user = User("Dude Doe", "username")

  override def login(username: String, password: String) = Future {
    Some(Token("", ""))
  }

  override def refresh(user: User) = Future {
    Token("", "")
  }

  override def validate(token: String) = Future {
    Some(user)
  }
}
