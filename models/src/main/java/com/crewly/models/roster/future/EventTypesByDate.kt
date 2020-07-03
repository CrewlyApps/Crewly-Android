package com.crewly.models.roster.future

import com.crewly.models.duty.DutyType
import org.joda.time.DateTime

data class EventTypesByDate(
  val date: DateTime,
  val events: List<DutyType>
)