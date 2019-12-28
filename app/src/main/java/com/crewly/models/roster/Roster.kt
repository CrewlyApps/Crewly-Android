package com.crewly.models.roster

import com.crewly.models.duty.Duty
import com.crewly.persistence.sector.Sector

/**
 * Created by Derek on 09/05/2019
 */
data class Roster(
  val duties: List<Duty>,
  val sectors: List<Sector>
)