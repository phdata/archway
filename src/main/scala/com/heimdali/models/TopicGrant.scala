package com.heimdali.models

import java.time.Instant

import cats._
import cats.data._
import cats.implicits._
import cats.effect.Effect
import com.heimdali.tasks.{GrantTopicAccess, ProvisionTask}
import com.heimdali.tasks.ProvisionTask._

import io.circe._
import io.circe.syntax._
import io.circe.java8.time._

case class TopicGrant(name: String,
                      ldapRegistration: LDAPRegistration,
                      actions: String,
                      id: Option[Long] = None,
                      topicAccess: Option[Instant] = None)

object TopicGrant {

  implicit val viewer: Show[TopicGrant] =
    Show.show(t => s"creating topic role")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, TopicGrant] =
    ProvisionTask.instance { grant =>
      for {
        ldap <- grant.ldapRegistration.provision
        access <- GrantTopicAccess(grant.id.get, grant.name, grant.ldapRegistration.sentryRole, NonEmptyList.fromListUnsafe(grant.actions.split(",").toList)).provision
      } yield access |+| ldap
    }

  implicit val encoder: Encoder[TopicGrant] =
    Encoder.instance { g =>
      Json.obj(
        "name" -> g.name.asJson,
        "group" -> g.ldapRegistration.asJson,
        "actions" -> g.actions.asJson,
        "id" -> g.id.asJson,
        "topic_access" -> g.topicAccess.asJson
      )
    }

  implicit val decoder: Decoder[TopicGrant] =
    Decoder.instance { g =>
      for {
        name <- g.downField("name").as[String]
        group <- g.downField("group").as[LDAPRegistration]
        actions <- g.downField("actions").as[String]
        id <- g.downField("id").as[Option[Long]]
        access <- g.downField("topic_access").as[Option[Instant]]
      } yield TopicGrant(name, group, actions, id, access)
    }

}
