package com.heimdali.services

trait LoginContextProvider {
  def kinit(): Unit
}
