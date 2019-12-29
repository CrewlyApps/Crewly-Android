package com.crewly.models.crew

import com.crewly.models.Company
import com.crewly.models.Rank
import org.joda.time.DateTime

data class Crew(
  val id: String = "",
  val name: String = "",
  val company: Company,
  val rank: Rank
)