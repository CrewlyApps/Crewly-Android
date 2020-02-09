package com.crewly.models.account

enum class CrewType(
  val type: String
) {
  CABIN("cabin"),
  FLIGHT("flight");

  companion object {
    fun fromType(
      type: String
    ): CrewType =
      when (type) {
        "cabin" -> CABIN
        else -> FLIGHT
      }
  }
}