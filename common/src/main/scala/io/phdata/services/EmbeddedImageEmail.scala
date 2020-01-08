package io.phdata.services

import courier.Multipart
import javax.mail.internet.MimeBodyPart

import scala.collection.mutable.ListBuffer

object EmbeddedImageEmail {

  def create(htmlContent: String, imageInfos: List[(String, String)]): Multipart = {
    val htmlPart = new MimeBodyPart()
    var bodyParts = new ListBuffer[MimeBodyPart]()

    htmlPart.setText(htmlContent, "UTF-8", "html")
    bodyParts += htmlPart
    for ((imageUrl, cid) <- imageInfos) {
      var imagePart: MimeBodyPart = new MimeBodyPart()
      imagePart.attachFile(imageUrl)
      imagePart.setContentID(s"<$cid>")
      imagePart.setDisposition("INLINE")
      bodyParts += imagePart
    }
    Multipart(bodyParts.toList)
  }
}
