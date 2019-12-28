package com.crewly.network.roster

data class NetworkFlight(
  val type: String,
  val code: String,
  val airline: String,
  val number: String,
  val start: String,
  val from: String,
  val end: String,
  val to: String,
  val isDeadHeaded: Boolean
)