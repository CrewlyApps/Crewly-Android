package com.crewly.models.duty

/**
 * Created by Derek on 01/06/2019
 */
data class DutyType(
  val name: String
) {

  companion object {
    const val ANNUAL_LEAVE = "A/L"
    const val AIRPORT_STANDBY = "SBY"
    const val BANK_HOLIDAY = "B/HOL"
    const val FLIGHT = "FLIGHT"
    const val HOME_STANDBY = "HSBY"
    const val NOT_AVAILABLE = "N/A"
    const val OFF = "OFF"
    const val PARENTAL_LEAVE = "P/L"
    const val SICK = "SICK"
    const val SPECIAL_EVENT = "SP/E"
    const val UNKNOWN = "UNKNOWN"
    const val UNPAID_LEAVE = "U/L"
  }

  fun isAirportStandby() = name == AIRPORT_STANDBY
  fun isHomeStandby() = name == HOME_STANDBY
  fun isFlight() = name == FLIGHT
  fun isOff() = name == OFF || name == BANK_HOLIDAY
  fun isAnnualLeave() = name == ANNUAL_LEAVE
  fun isSick() = name == SICK
  fun isParentalLeave() = name == PARENTAL_LEAVE
  fun isSpecialEvent() = name == SPECIAL_EVENT
}