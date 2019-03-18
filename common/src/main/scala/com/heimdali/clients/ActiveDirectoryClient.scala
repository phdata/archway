package com.heimdali.clients

import com.unboundid.ldap.sdk.SearchResultEntry

trait ActiveDirectoryClient[F[_]] { this: LDAPClientImpl[F] =>

  override def searchQuery(username: String): String =
    s"(sAMAccountName=$username)"

  override def fullUsername(username: String): String =
    s"$username@${ldapConfig.realm}"

  override def ldapUser(searchResultEntry: SearchResultEntry) = 
    LDAPUser(s"${searchResultEntry.getAttributeValue("cn")}",
      searchResultEntry.getAttributeValue("sAMAccountName"),
      searchResultEntry.getDN,
      Option(searchResultEntry.getAttributeValues("memberOf")).map(_.toSeq).getOrElse(Seq.empty),
      Option(searchResultEntry.getAttributeValue("mail")))

  override def groupObjectClass: String =
    "group"
}
