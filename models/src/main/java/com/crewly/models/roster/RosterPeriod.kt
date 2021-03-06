package com.crewly.models.roster

import com.crewly.models.duty.Duty
import com.crewly.models.flight.Flight
import org.joda.time.DateTime

/**
 * Created by Derek on 02/06/2018
 */
sealed class RosterPeriod {

  data class RosterDate(
    var date: DateTime = DateTime(),
    var flights: MutableList<Flight> = mutableListOf(),
    val duties: List<Duty> = listOf()
  )

  data class RosterWeek(var rosterDates: List<RosterDate> = listOf())
  data class RosterMonth(var rosterDates: MutableList<RosterDate> = mutableListOf())
}