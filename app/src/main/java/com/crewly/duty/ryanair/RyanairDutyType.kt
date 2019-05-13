package com.crewly.duty.ryanair

/**
 * Created by Derek on 19/08/2018
 * The different types of duty a Ryanair roster can have.
 */
enum class RyanairDutyType(
  var dutyName: String
) {

  ANNUAL_LEAVE("A/L"),
  AIRPORT_STANDBY("SBY"),
  BANK_HOLIDAY("B/HOL"),
  FLIGHT("FLIGHT"),
  HOME_STANDBY("HSBY"),
  NOT_AVAILABLE("N/A"),
  OFF("OFF"),
  PARENTAL_LEAVE("P/L"),
  SICK("SICK"),
  SPECIAL_EVENT("SP/E"),
  UNKNOWN("UNKNOWN"),
  UNPAID_LEAVE("U/L")
}