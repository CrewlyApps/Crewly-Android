package com.crewly.account

import androidx.room.*
import com.crewly.crew.Rank
import com.crewly.models.Company
import com.crewly.salary.Salary
import org.joda.time.DateTime

/**
 * Created by Derek on 11/06/2018
 */
@Entity(tableName = "accounts")
data class Account(
  @PrimaryKey
  @ColumnInfo(name = "crew_code") var crewCode: String = "",

  var name: String = "",

  var company: Company = Company.None,

  var base: String = "",

  var rank: Rank = Rank.NONE,

  @ColumnInfo(name = "is_pilot")
  var isPilot: Boolean = false,

  @ColumnInfo(name = "joined_company_at")
  var joinedCompanyAt: DateTime = DateTime(0),

  @ColumnInfo(name = "show_crew")
  var showCrew: Boolean = false,

  @ColumnInfo(name = "update_sectors_real_time_enabled")
  var updateSectorsRealTimeEnabled: Boolean = false,

  @Embedded
  var salary: Salary = Salary()) {

  @Ignore
  constructor(): this("")
}