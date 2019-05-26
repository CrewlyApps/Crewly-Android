package com.crewly.models.roster

import com.crewly.db.duty.Duty
import com.crewly.db.sector.Sector
import org.joda.time.DateTime

/**
 * Created by Derek on 02/06/2018
 */
sealed class RosterPeriod {

  data class RosterDate(
    var date: DateTime = DateTime(),
    var duties: MutableList<Duty> = mutableListOf(),
    var sectors: MutableList<Sector> = mutableListOf()
  )

  data class RosterWeek(var rosterDates: List<RosterDate> = listOf())
  data class RosterMonth(var rosterDates: MutableList<RosterDate> = mutableListOf())
}