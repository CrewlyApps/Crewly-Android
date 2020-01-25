package com.crewly.models.sector

import com.crewly.models.Company
import com.crewly.models.airport.Airport
import org.joda.time.DateTime
import org.joda.time.Period

data class Sector(
  val flightId: String = "",
  var arrivalAirport: Airport = Airport(),
  var departureAirport: Airport = Airport(),
  var arrivalTime: DateTime = DateTime(),
  var departureTime: DateTime = DateTime(),
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