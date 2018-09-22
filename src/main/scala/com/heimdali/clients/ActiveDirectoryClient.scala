package com.heimdali.clients

import com.unboundid.ldap.sdk.SearchResultEntry

trait ActiveDirectoryClient[F[_]] { this: LDAPClientImpl[F] =>

  override def searchQuery(username: String): String =
    s"(sAMAccountName=$username)"

  override def fullUsername(username: String): String =
    s"$username@${sys.env.getOrElse("HEIMDALI_REALM", "JOTUNN.IO")}"

  override def ldapUser(searchResultEntry: SearchResultEntry) =
    LDAPUser(s"${searchResultEntry.getAttributeValue("cn")}",
      searchResultEntry.getAttributeValue("sAMAccountName"),
      searchResultEntry.getAttributeValues("memberOf"),
      Option(searchResultEntry.getAttributeValue("mail")))

  override def groupObjectClass: String =
    "group"
}
