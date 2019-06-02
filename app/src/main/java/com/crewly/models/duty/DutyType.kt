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
}