package com.heimdali.services

import com.unboundid.ldap.sdk.SearchResultEntry

trait OpenLDAPClient {
  this: LDAPClientImpl =>

  override def searchQuery(username: String): String =
    s"(cn=$username)"

  override def fullUsername(username: String): String =
    s"cn=$username,$usersPath,$baseDN"

  override def ldapUser(searchResultEntry: SearchResultEntry) =
    LDAPUser(s"${searchResultEntry.getAttributeValue("givenname")} ${searchResultEntry.getAttributeValue("sn")}",
      searchResultEntry.getAttributeValue("cn"),
      Seq.empty[String])

  override def groupObjectClass: String =
    "groupOfNames"
}
