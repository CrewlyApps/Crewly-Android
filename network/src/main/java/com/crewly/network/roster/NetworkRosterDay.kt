package com.crewly.network.roster

data class NetworkRosterDay(
  val date: String,
  val events: List<NetworkEvent> = emptyList(),
  val flights: List<NetworkFlight> = emptyList(),
  val crew: List<NetworkCrew> = emptyList()
)