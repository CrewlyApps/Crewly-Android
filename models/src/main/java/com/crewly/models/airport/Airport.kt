package com.crewly.models.airport

data class Airport(
  val codeIata: String = "",
  val codeIcao: String = "",
  val name: String = "",
  val city: String = "",
  val country: String = "",
  val timezone: String = "",
  val latitude: Float = 0f,
  val longitude: Float = 0f
)