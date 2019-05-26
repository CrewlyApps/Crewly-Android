package com.crewly.roster

import com.crewly.db.duty.Duty
import com.crewly.duty.Sector

/**
 * Created by Derek on 09/05/2019
 */
data class Roster(
  val duties: List<Duty>,
  val sectors: List<Sector>
)