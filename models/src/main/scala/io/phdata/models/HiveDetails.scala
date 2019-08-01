package io.phdata.models

case class HiveDatabase(name: String, tables: List[HiveTable])
case class HiveTable(name: String)
