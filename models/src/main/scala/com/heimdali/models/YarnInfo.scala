package com.heimdali.models

import io.circe._
import io.circe.syntax._

case class YarnApplication(id: String, name: String)

object YarnApplication {
  implicit val encoder: Encoder[YarnApplication] =
    Encoder.instance { r =>
      Json.obj(
        "id" -> r.id.asJson,
        "name" -> r.name.asJson
      )
    }

  implicit val decoder: Decoder[YarnApplication] =
    Decoder.forProduct2("applicationId", "name")((id, name) => YarnApplication(id, name))
}

case class YarnInfo(resourcePool: String, applications: List[YarnApplication])

object YarnInfo {
  implicit val encoder: Encoder[YarnInfo] =
    Encoder.instance { r =>
      Json.obj(
        "resource_pool" -> r.resourcePool.asJson,
        "applications" -> r.applications.asJson
      )
    }
}

case class YarnApplicationList(applications: List[YarnApplication])

object YarnApplicationList {
  implicit val decoder: Decoder[YarnApplicationList] =
    Decoder.forProduct1("applications")((list) => YarnApplicationList(list))
}
