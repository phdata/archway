package io.phdata.services

import courier.Multipart
import javax.mail.internet.MimeBodyPart

object EmbeddedImageEmail {

  def create(htmlContent: String, imageUrl: String, cid: String): Multipart = {
    val htmlPart = new MimeBodyPart()
    htmlPart.setText(htmlContent, "UTF-8", "html")

    val imagePart: MimeBodyPart = new MimeBodyPart()
    imagePart.attachFile(imageUrl)
    imagePart.setContentID(s"<$cid>")
    imagePart.setDisposition("INLINE")
    Multipart(List(htmlPart, imagePart))
  }
}
