package io.phdata.models

import io.circe._, io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

case class CustomLink(
    name: String,
    description: String,
    url: String,
    customLinkGroupId: Option[Long] = None,
    id: Option[Long] = None
) {
  assert(name.nonEmpty)
  assert(description.nonEmpty)
  assert(url.nonEmpty)
}

object CustomLink {

  implicit val encoder: Encoder[CustomLink] =
    Encoder.instance { l =>
      Json.obj(
        "id" -> l.id.asJson,
        "name" -> l.name.asJson,
        "description" -> l.description.asJson,
        "url" -> l.url.asJson,
        "customLinkGroupId" -> l.customLinkGroupId.asJson,
        "id" -> l.id.asJson
      )
    }

  implicit val decoder: Decoder[CustomLink] =
    Decoder.instance { l =>
      for {
        id <- l.downField("id").as[Option[Long]]
        name <- l.downField("name").as[String]
        description <- l.downField("description").as[String]
        url <- l.downField("url").as[String]
        customLinkGroupId <- l.downField("customLinkGroupId").as[Option[Long]]
      } yield CustomLink(name, description, url, customLinkGroupId, id)
    }
}
