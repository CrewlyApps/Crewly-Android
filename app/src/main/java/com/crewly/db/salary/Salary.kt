package com.crewly.db.salary

import androidx.room.ColumnInfo

/**
 * Created by Derek on 06/08/2018
 * Represents the salary information for a user.
 */
data class Salary(
  @ColumnInfo(name = "per_month_base")
  var perMonthBase: Float = 0f,

  @ColumnInfo(name = "per_flight_hour")
  var perFlightHour: Float = 0f,

  @ColumnInfo(name = "per_flight_hour_oob")
  var perFlightHourOob: Float = 0f,

  @ColumnInfo(name = "per_asby_hour")
  var perAsbyHour: Float = 0f,

  @ColumnInfo(name = "per_hsby_hour")
  var perHsbyHour: Float = 0f
) {

  fun hasSalaryInfo(): Boolean =
    perMonthBase > 0f || perFlightHour > 0f || perFlightHourOob > 0f ||
      perAsbyHour > 0f || perHsbyHour > 0f
}