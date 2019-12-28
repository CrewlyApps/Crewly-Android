package com.crewly.network.roster

data class NetworkEvent(
  val type: String,
  val code: String,
  val time: String,
  val location: String
)