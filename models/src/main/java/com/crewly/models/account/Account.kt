package com.crewly.models.account

import com.crewly.models.Company
import com.crewly.models.Salary
import org.joda.time.DateTime

data class Account(
  val crewCode: String = "",
  var name: String = "",
  var company: Company = Company.None,
  var base: String = "",
  val joinedCompanyAt: DateTime = DateTime(0),
  val updateSectorsRealTimeEnabled: Boolean = false,
  val salary: Salary = Salary()
)