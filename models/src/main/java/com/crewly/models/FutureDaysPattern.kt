package com.crewly.models

data class FutureDaysPattern(
  val firstNumberOfDaysOn: Int = CREW_CONSECUTIVE_DAYS_ON,
  val firstNumberOfDaysOff: Int = CREW_CONSECUTIVE_DAYS_OFF,
  val secondNumberOfDaysOn: Int = CREW_CONSECUTIVE_DAYS_ON,
  val secondNumberOfDaysOff: Int = CREW_CONSECUTIVE_DAYS_OFF
) {

  companion object {
    private const val CREW_CONSECUTIVE_DAYS_ON = 5
    private const val CREW_CONSECUTIVE_DAYS_OFF = 3

    private const val PILOT_CONSECUTIVE_DAYS_ON = 6
    private const val PILOT_CONSECUTIVE_DAYS_OFF = 4
  }
}