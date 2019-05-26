package com.crewly.db.sector

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.crewly.models.Company
import org.joda.time.DateTime
import org.joda.time.Period

/**
 * Created by Derek on 14/06/2018
 * @param ownerId The id of the user who's roster this sector belongs to.
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
  val flightId: String = "",

  @ColumnInfo(name = "arrival_airport")
  var arrivalAirport: String = "",

  @ColumnInfo(name = "departure_airport")
  var departureAirport: String = "",

  @ColumnInfo(name = "arrival_time")
  var arrivalTime: DateTime = DateTime(),

  @ColumnInfo(name = "departure_time")
  var departureTime: DateTime = DateTime(),

  @ColumnInfo(name = "owner_id")
  var ownerId: String = "",

  var company: Company = Company.None,

  var crew: MutableList<String> = mutableListOf()
) {

  override fun equals(other: Any?): Boolean {
    return other != null && other is Sector
      && other.flightId == flightId
      && other.departureAirport == departureAirport
      && other.arrivalAirport == arrivalAirport
  }

  override fun hashCode(): Int {
    return flightId.hashCode() +
      departureAirport.hashCode() +
      arrivalAirport.hashCode()
  }

  fun getFlightDuration(): Period = Period(departureTime, arrivalTime)

  /**
   * Check whether [sector] is a return flight for this sector.
   */
  fun isReturnFlight(sector: Sector): Boolean =
    departureAirport == sector.arrivalAirport && arrivalAirport == sector.departureAirport
}