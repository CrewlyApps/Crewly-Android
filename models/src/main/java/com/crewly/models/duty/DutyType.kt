package com.crewly.models.duty

/**
 * Created by Derek on 01/06/2019
 */
data class DutyType(
  val name: String,
  val code: String
) {

  companion object {
    const val TYPE_AIRPORT_STANDBY = "asby"
    const val TYPE_CHECK_IN = "check_in"
    const val TYPE_CHECK_OUT = "check_out"
    const val TYPE_CLEAR = "clear"
    const val TYPE_HOTEL = "hotel"
    const val TYPE_HOME_STANDBY = "hsby"
    const val TYPE_OFF = "off"
    const val TYPE_PICK_UP = "pick_up"
    const val TYPE_REST_ON_DUTY = "rest_on_duty"
    const val TYPE_SPECIAL = "special"

    const val ANNUAL_LEAVE = "A/L"
    const val PARENTAL_LEAVE = "P/L"
    const val SICK = "SICK"
  }

  fun isAirportStandby() = name == TYPE_AIRPORT_STANDBY
  fun isCheckIn() = name == TYPE_CHECK_IN
  fun isCheckOut() = name == TYPE_CHECK_OUT
  fun isClear() = name == TYPE_CLEAR
  fun isHotel() = name == TYPE_HOTEL
  fun isHomeStandby() = name == TYPE_HOME_STANDBY
  fun isOff() = name == TYPE_OFF
  fun isPickUp() = name == TYPE_PICK_UP
  fun isRestOnDuty() = name == TYPE_REST_ON_DUTY
  fun isSpecial() = name == TYPE_SPECIAL

  fun isAnnualLeave() = name == ANNUAL_LEAVE
  fun isSick() = name == SICK
  fun isParentalLeave() = name == PARENTAL_LEAVE
}