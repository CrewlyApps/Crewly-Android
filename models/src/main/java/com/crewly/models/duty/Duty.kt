package com.crewly.models.duty

import com.crewly.models.Company
import com.crewly.models.airport.Airport
import org.joda.time.DateTime

data class Duty(
  val id: Long = 0,
  val ownerId: String = "",
  val company: Company = Company.None,
  val type: DutyType = DutyType("", ""),
  val startTime: DateTime = DateTime(0),
  val endTime: DateTime = DateTime(0),
  val from: Airport,
  val to: Airport,
  val phoneNumber: String
)