package com.crewly.network.roster

data class NetworkEvent(
  val type: String = "",
  val code: String = "",
  val time: String = "",
  val start: String = "",
  val end: String = "",
  val from: String = "",
  val to: String = "",
  val phoneNumber: String = ""
)