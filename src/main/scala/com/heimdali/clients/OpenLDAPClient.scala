package com.heimdali.clients

import com.unboundid.ldap.sdk.SearchResultEntry

trait OpenLDAPClient {
  this: LDAPClientImpl =>
  val userPath: String = ldapConfiguration.getString("users_path")

  override def searchQuery(username: String): String =
    s"(cn=$username)"

  override def fullUsername(username: String): String =
    s"cn=$username,$userPath"

  override def ldapUser(searchResultEntry: SearchResultEntry) =
    LDAPUser(s"${searchResultEntry.getAttributeValue("givenname")} ${searchResultEntry.getAttributeValue("sn")}",
      searchResultEntry.getAttributeValue("cn"),
      searchResultEntry.getAttributeValues("memberOf"))

  override def groupObjectClass: String =
    "group"
}
