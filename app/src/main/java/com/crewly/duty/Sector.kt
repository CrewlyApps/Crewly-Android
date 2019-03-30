package com.crewly.duty

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import org.joda.time.DateTime
import org.joda.time.Period

/**
 * Created by Derek on 14/06/2018
 * @param crewCode The id of the user who's roster this sector belongs to.
 * @param crew A list of all crew members for this sector
 */
@Entity(
  tableName = "sectors",
  primaryKeys = [
    "flight_id",
    "departure_time",
    "departure_airport",
    "arrival_airport"
  ],
  indices = [(Index("departure_time"))]
)
data class Sector(
  @ColumnInfo(name = "flight_id")
  var flightId: String = "",

  @ColumnInfo(name = "arrival_airport")
  var arrivalAirport: String = "",

  @ColumnInfo(name = "departure_airport")
  var departureAirport: String = "",

  @ColumnInfo(name = "arrival_time")
  var arrivalTime: DateTime = DateTime(),

  @ColumnInfo(name = "departure_time")
  var departureTime: DateTime = DateTime(),

  @ColumnInfo(name = "crew_code")
  var crewCode: String = "",

  var crew: MutableList<String> = mutableListOf()
) {

  @Ignore
  constructor(): this("")

  fun getFlightDuration(): Period = Period(departureTime, arrivalTime)

  /**
   * Check whether [sector] is a return flight for this sector.
   */
  fun isReturnFlight(sector: Sector): Boolean =
    departureAirport == sector.arrivalAirport && arrivalAirport == sector.departureAirport
}