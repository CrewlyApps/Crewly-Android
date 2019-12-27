package com.crewly.models.duty

/**
 * Created by Derek on 01/06/2019
 */
interface DutyType {

  val name: String

  fun isAirportStandby(): Boolean
  fun isHomeStandby(): Boolean
  fun isFlight(): Boolean
  fun isOff(): Boolean
  fun isAnnualLeave(): Boolean
  fun isSick(): Boolean
  fun isParentalLeave(): Boolean
  fun isSpecialEvent(): Boolean
}