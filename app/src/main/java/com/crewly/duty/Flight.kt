package com.crewly.duty

import com.crewly.db.airport.Airport

/**
 * Created by Derek on 26/07/2018
 */
data class Flight(
  var departureSector: Sector = Sector(),
  var departureAirport: Airport = Airport(),
  var arrivalSector: Sector = Sector(),
  var arrivalAirport: Airport = Airport()
)