package com.heimdali.controller

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding

object Credentials {
  def unapply(header: String): Option[(String, String)] = {
    header.replace("Basic ", "") match {
      case possible if BaseEncoding.base64().canDecode(possible) =>
        new String(BaseEncoding.base64().decode(possible), Charsets.UTF_8).split(":").toList match {
          case username :: password :: Nil => Some((username, password))
          case _ => None
        }
      case _ => None
    }
  }
}

case class Credentials(username: String, password: String)