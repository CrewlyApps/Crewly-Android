package com.crewly.network.roster

data class NetworkRoster(
  val days: List<NetworkRosterDay>,
  val raw: NetworkRosterRaw
)