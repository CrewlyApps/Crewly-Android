package com.crewly.models

/**
 * Created by Derek on 06/08/2018
 * Represents the salary information for a user.
 */
data class Salary(
  var perMonthBase: Float = 0f,
  var perFlightHour: Float = 0f,
  var perFlightHourOob: Float = 0f,
  var perAsbyHour: Float = 0f,
  var perHsbyHour: Float = 0f
) {

  fun hasSalaryInfo(): Boolean =
    perMonthBase > 0f || perFlightHour > 0f || perFlightHourOob > 0f ||
      perAsbyHour > 0f || perHsbyHour > 0f
}