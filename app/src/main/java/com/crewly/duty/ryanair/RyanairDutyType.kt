package com.crewly.duty.ryanair

import com.crewly.models.duty.DutyType

/**
 * Created by Derek on 01/06/2019
 */
class RyanairDutyType(
  override val name: String
): DutyType {

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

  override fun isAirportStandby() = name == AIRPORT_STANDBY
  override fun isHomeStandby() = name == HOME_STANDBY
  override fun isFlight() = name == FLIGHT
  override fun isOff() = name == OFF
  override fun isAnnualLeave() = name == ANNUAL_LEAVE
  override fun isSick() = name == SICK
  override fun isParentalLeave() = name == PARENTAL_LEAVE
  override fun isSpecialEvent() = name == SPECIAL_EVENT
}