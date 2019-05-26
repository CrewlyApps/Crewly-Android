package com.crewly.db.account

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
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
  @ColumnInfo(name = "crew_code") val crewCode: String = "",

  var name: String = "",

  var company: Company = Company.None,

  var base: String = "",

  val rank: Rank = Rank.NONE,

  @ColumnInfo(name = "is_pilot")
  var isPilot: Boolean = false,

  @ColumnInfo(name = "joined_company_at")
  val joinedCompanyAt: DateTime = DateTime(0),

  @ColumnInfo(name = "show_crew")
  val showCrew: Boolean = true,

  @ColumnInfo(name = "update_sectors_real_time_enabled")
  val updateSectorsRealTimeEnabled: Boolean = false,

  @Embedded
  val salary: Salary = Salary()
)