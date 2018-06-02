package com.crewly.roster

import com.crewly.sector.Sector
import org.joda.time.DateTime

/**
 * Created by Derek on 27/05/2018
 */
data class RosterDate(var date: DateTime = DateTime(),
                      var rosterType: RosterType = RosterType.Duty,
                      var sectors: List<Sector> = listOf())