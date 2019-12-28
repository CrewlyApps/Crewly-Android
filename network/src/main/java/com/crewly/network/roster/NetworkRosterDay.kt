package com.crewly.network.roster

data class NetworkRosterDay(
  val date: String,
  val events: List<NetworkEvent>,
  val flights: List<NetworkFlight>
)