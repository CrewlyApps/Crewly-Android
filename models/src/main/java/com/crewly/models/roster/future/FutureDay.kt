package com.crewly.models.roster.future

import com.crewly.models.duty.DutyType
import org.joda.time.DateTime

data class FutureDay(
  val date: DateTime,
  val type: DutyType
)