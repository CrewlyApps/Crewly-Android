package com.crewly.roster.details

import com.crewly.models.roster.RosterPeriod

data class RosterDetailsSummaryViewData(
  val rosterDate: RosterPeriod.RosterDate,
  val code: String,
  val checkInTime: String,
  val checkOutTime: String
)