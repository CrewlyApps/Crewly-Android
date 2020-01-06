package com.crewly.network.roster

data class NetworkRoster(
  val days: List<NetworkRosterDay> = emptyList(),
  val raw: NetworkRawRoster = NetworkRawRoster()
)