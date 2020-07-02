package com.crewly.models.account

import com.crewly.models.Company
import com.crewly.models.roster.future.FutureDaysPattern
import com.crewly.models.Salary
import org.joda.time.DateTime

data class Account(
  val crewCode: String = "",
  var name: String = "",
  var company: Company = Company.None,
  val crewType: String = "",
  var base: String = "",
  val joinedCompanyAt: DateTime = DateTime(0),
  val updateFlightsRealTimeEnabled: Boolean = false,
  val salary: Salary = Salary(),
  val futureDaysPattern: FutureDaysPattern = FutureDaysPattern()
)