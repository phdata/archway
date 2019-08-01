package io.phdata.services

import cats.data.OptionT
import io.phdata.models.{MemberRoleRequest, WorkspaceMemberEntry}
import io.circe._
import io.circe.syntax._

trait MemberService[F[_]] {

  def members(id: Long): F[List[WorkspaceMemberEntry]]

  def addMember(id: Long, request: MemberRoleRequest): OptionT[F, WorkspaceMemberEntry]

  def removeMember(id: Long, request: MemberRoleRequest): OptionT[F, WorkspaceMemberEntry]

  def availableMembers(filter: String): F[MemberSearchResult]

}

case class MemberSearchResult(users: List[MemberSearchResultItem], groups: List[MemberSearchResultItem])

object MemberSearchResult {

  implicit val encoder: Encoder[MemberSearchResult] = Encoder.instance { cursor =>
    Json.obj(
      "users" -> cursor.users.asJson,
      "groups" -> cursor.groups.asJson
    )
  }

}

case class MemberSearchResultItem(display: String, commonName: String)

object MemberSearchResultItem {

  implicit val encoder: Encoder[MemberSearchResultItem] = Encoder.instance { cursor =>
    Json.obj(
      "display" -> cursor.display.asJson,
      "distinguished_name" -> cursor.commonName.asJson
    )
  }

}
