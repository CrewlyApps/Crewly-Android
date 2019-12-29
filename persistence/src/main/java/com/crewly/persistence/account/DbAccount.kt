package com.crewly.persistence.account

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.crewly.models.Salary

/**
 * Created by Derek on 11/06/2018
 */
@Entity(tableName = "accounts")
data class DbAccount(

  @PrimaryKey val crewCode: String = "",
  val name: String = "",
  val companyId: Int,
  val base: String = "",
  val isPilot: Boolean = false,
  val joinedCompanyAt: Long,
  val showCrew: Boolean = true,
  val updateSectorsRealTimeEnabled: Boolean = false,
  @Embedded val salary: Salary = Salary()
)