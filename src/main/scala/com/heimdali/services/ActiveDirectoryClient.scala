package com.heimdali.services

import com.unboundid.ldap.sdk.SearchResultEntry

trait ActiveDirectoryClient { this: LDAPClientImpl =>

  override def searchQuery(username: String): String =
    s"(sAMAccountName=$username)"

  override def fullUsername(username: String): String =
    s"$username@CORP.JOTUNN.IO"

  override def ldapUser(searchResultEntry: SearchResultEntry) =
    LDAPUser(s"${searchResultEntry.getAttributeValue("cn")}",
      searchResultEntry.getAttributeValue("sAMAccountName"),
      Seq.empty[String])

  override def groupObjectClass: String =
    "group"
}
