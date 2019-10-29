package io.phdata.clients

import cats.data.OptionT
import cats.effect.Effect
import com.typesafe.scalalogging.LazyLogging
import io.phdata.config
import io.phdata.config.{LDAPBinding, LDAPConfig}
import io.phdata.models.DistinguishedName
import io.phdata.services.MemberSearchResult

class LDAPClientAlternativeImpl[F[_]: Effect](ldapConfig: LDAPConfig, binding: LDAPConfig => LDAPBinding)
  extends LDAPClient[F] with LazyLogging {
  override def findUserByDN(distinguishedName: DistinguishedName): OptionT[F, LDAPUser] = throw new NotImplementedError()

  override def findUserByUserName(username: String): OptionT[F, LDAPUser] = throw new NotImplementedError()

  override def validateUser(username: String, password: config.Password): OptionT[F, LDAPUser] = throw new NotImplementedError()

  override def createGroup(groupName: String, attributes: List[(String, String)]): F[Unit] = throw new NotImplementedError()

  override def deleteGroup(groupDN: DistinguishedName): OptionT[F, String] = throw new NotImplementedError()

  override def addUserToGroup(groupName: DistinguishedName, distinguishedName: DistinguishedName): OptionT[F, String] = throw new NotImplementedError()

  override def removeUserFromGroup(groupName: DistinguishedName, distinguishedName: DistinguishedName): OptionT[F, String] = throw new NotImplementedError()

  override def groupMembers(groupDN: DistinguishedName): F[List[LDAPUser]] = throw new NotImplementedError()

  override def search(filter: String): F[MemberSearchResult] = throw new NotImplementedError()
}
