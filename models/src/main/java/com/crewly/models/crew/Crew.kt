package com.crewly.models.crew

import com.crewly.models.Company

data class Crew(
  val id: String = "",
  val name: String = "",
  val company: Company,
  val rank: String
)