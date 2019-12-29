package com.crewly.network.roster

data class NetworkCrew(
  val name: String,
  val lastName: String,
  val fullName: String,
  val rank: String,
  val position: Int
) {

  override fun equals(other: Any?): Boolean =
    other != null && other is NetworkCrew && other.fullName == fullName

  override fun hashCode(): Int = fullName.hashCode()
}