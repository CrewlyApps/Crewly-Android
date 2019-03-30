package com.crewly.salary

import androidx.room.ColumnInfo
import androidx.room.Ignore

/**
 * Created by Derek on 06/08/2018
 * Represents the salary information for a user.
 */
data class Salary(
  @ColumnInfo(name = "base_salary")
  var base: Float = 0f,

  @ColumnInfo(name = "per_flight_hour")
  var perFlightHour: Float = 0f,

  @ColumnInfo(name = "per_flight_hour_oob")
  var perFlightHourOob: Float = 0f,

  @ColumnInfo(name = "per_asby_hour")
  var perAsbyHour: Float = 0f,

  @ColumnInfo(name = "per_hsby_hour")
  var perHsbyHour: Float = 0f) {

  @Ignore
  constructor(): this(0f)

  /**
   * Check whether there is any salary information saved. Will return true for empty if nothing
   * is saved.
   */
  fun isEmpty(): Boolean =
    base == 0f && perFlightHour == 0f && perFlightHourOob == 0f &&
      perAsbyHour == 0f && perHsbyHour == 0f
}