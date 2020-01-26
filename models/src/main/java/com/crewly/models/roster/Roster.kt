package com.crewly.models.roster

import com.crewly.models.duty.Duty
import com.crewly.models.flight.Flight

/**
 * Created by Derek on 09/05/2019
 */
data class Roster(
  val duties: List<Duty>,
  val flights: List<Flight>
)