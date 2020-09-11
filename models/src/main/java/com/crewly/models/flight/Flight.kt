package com.crewly.models.flight

import com.crewly.models.Company
import com.crewly.models.airport.Airport
import org.joda.time.DateTime

data class Flight(
  val flightId: String = "",
  var arrivalAirport: Airport = Airport(),
  var departureAirport: Airport = Airport(),
  var arrivalTime: DateTime = DateTime(),
  var departureTime: DateTime = DateTime(),
  var ownerId: String = "",
  var company: Company = Company.None,
  var crew: MutableList<String> = mutableListOf()
) {

  override fun equals(other: Any?): Boolean =
    other != null && other is Flight
      && other.flightId == flightId
      && other.departureAirport == departureAirport
      && other.arrivalAirport == arrivalAirport

  override fun hashCode(): Int =
    flightId.hashCode() +
      departureAirport.hashCode() +
      arrivalAirport.hashCode()

  /**
   * Check whether [flight] is a return flight for this flight.
   */
  fun isReturnFlight(flight: Flight): Boolean =
    departureAirport == flight.arrivalAirport && arrivalAirport == flight.departureAirport
}