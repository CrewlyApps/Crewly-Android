package com.crewly.models.duty

import com.crewly.models.Company
import org.joda.time.DateTime

data class Duty(
  val id: Long = 0,
  var ownerId: String = "",
  var company: Company = Company.None,
  var type: DutyType = DutyType(""),
  var code: String = "",
  var startTime: DateTime = DateTime(0),
  var endTime: DateTime = DateTime(0),
  var location: String = ""
)