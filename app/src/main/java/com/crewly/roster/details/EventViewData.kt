package com.crewly.roster.details

import com.crewly.models.duty.Duty

data class EventViewData(
  val duty: Duty,
  val startTime: String,
  val endTime: String
)