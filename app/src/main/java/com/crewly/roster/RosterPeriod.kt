package com.crewly.roster

import com.crewly.duty.DutyType
import com.crewly.duty.Sector
import org.joda.time.DateTime

/**
 * Created by Derek on 02/06/2018
 */
sealed class RosterPeriod {

    data class RosterDate(var date: DateTime = DateTime(),
                          var dutyType: DutyType = DutyType(),
                          var sectors: List<Sector> = listOf())

    data class RosterWeek(var rosterDates: List<RosterDate> = listOf())
    data class RosterMonth(var rosterDates: List<RosterDate> = listOf())
}