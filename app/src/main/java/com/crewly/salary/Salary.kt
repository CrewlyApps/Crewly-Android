package com.crewly.salary

import android.arch.persistence.room.ColumnInfo

/**
 * Created by Derek on 06/08/2018
 */
data class Salary(@ColumnInfo(name = "base_salary")
                  var base: Float = 0f,

                  @ColumnInfo(name = "per_flight_hour")
                  var perFlightHour: Float = 0f,

                  @ColumnInfo(name = "per_flight_hour_oob")
                  var perFlightHourOob: Float = 0f,

                  @ColumnInfo(name = "per_asby_hour")
                  var perAsbyHour: Float = 0f,

                  @ColumnInfo(name = "per_hsby_hour")
                  var perHsbyHour: Float = 0f)