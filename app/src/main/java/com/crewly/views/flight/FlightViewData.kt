package com.crewly.views.flight

import com.crewly.models.flight.Flight

class FlightViewData(
  val flight: Flight,
  val arrivalTimeZulu: String,
  val arrivalTimeLocal: String,
  val departureTimeZulu: String,
  val departureTimeLocal: String,
  val duration: String
)