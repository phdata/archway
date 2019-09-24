package io.phdata.models

import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

case class CustomLinkGroup(
    name: String,
    description: String,
    links: List[CustomLink],
    id: Option[Long] = None
) {
  assert(name.nonEmpty)
  assert(description.nonEmpty)
}

object CustomLinkGroup {

  implicit val encoder: Encoder[CustomLinkGroup] =
    Encoder.instance { g =>
      Json.obj(
        "id" -> g.id.asJson,
        "name" -> g.name.asJson,
        "description" -> g.description.asJson,
        "links" -> g.links.asJson
      )
    }

  implicit val decoder: Decoder[CustomLinkGroup] =
    Decoder.instance { g =>
      for {
        id <- g.downField("id").as[Option[Long]]
        name <- g.downField("name").as[String]
        description <- g.downField("description").as[String]
        links <- g.downField("links").as[List[CustomLink]]
      } yield CustomLinkGroup(name, description, links, id)
    }
}
