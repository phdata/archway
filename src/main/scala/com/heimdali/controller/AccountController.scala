package com.heimdali.controller

import javax.inject.{Inject, Singleton}

import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class AccountController @Inject() (controllerComponents: ControllerComponents)
  extends AbstractController(controllerComponents) {


}
