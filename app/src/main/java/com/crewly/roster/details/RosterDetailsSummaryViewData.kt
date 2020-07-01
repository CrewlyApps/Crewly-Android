package com.crewly.roster.details

import com.crewly.models.Company
import com.crewly.models.roster.RosterPeriod

data class RosterDetailsSummaryViewData(
  val company: Company,
  val rosterDate: RosterPeriod.RosterDate,
  val code: String,
  val checkInTime: String,
  val checkOutTime: String
)