package com.crewly.models

data class FutureDaysPattern(
  val firstNumberOfDaysOn: Int = 0,
  val firstNumberOfDaysOff: Int = 0,
  val secondNumberOfDaysOn: Int = 0,
  val secondNumberOfDaysOff: Int = 0
) {

  companion object {
    private const val CREW_CONSECUTIVE_DAYS_ON = 5
    private const val CREW_CONSECUTIVE_DAYS_OFF = 3

    private const val PILOT_CONSECUTIVE_DAYS_ON = 6
    private const val PILOT_CONSECUTIVE_DAYS_OFF = 4
  }

  fun hasPattern() =
    firstNumberOfDaysOn > 0 || firstNumberOfDaysOff > 0 ||
    secondNumberOfDaysOn > 0 || secondNumberOfDaysOff > 0

  fun toCrewPattern(): FutureDaysPattern =
    copy(
      firstNumberOfDaysOn = CREW_CONSECUTIVE_DAYS_ON,
      firstNumberOfDaysOff = CREW_CONSECUTIVE_DAYS_OFF,
      secondNumberOfDaysOn = CREW_CONSECUTIVE_DAYS_ON,
      secondNumberOfDaysOff = CREW_CONSECUTIVE_DAYS_OFF
    )

  fun toPilotPattern(): FutureDaysPattern =
    copy(
      firstNumberOfDaysOn = PILOT_CONSECUTIVE_DAYS_ON,
      firstNumberOfDaysOff = PILOT_CONSECUTIVE_DAYS_OFF,
      secondNumberOfDaysOn = PILOT_CONSECUTIVE_DAYS_ON,
      secondNumberOfDaysOff = PILOT_CONSECUTIVE_DAYS_OFF
    )
}