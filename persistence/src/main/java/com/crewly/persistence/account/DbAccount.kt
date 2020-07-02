package com.crewly.persistence.account

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.crewly.models.roster.future.FutureDaysPattern
import com.crewly.models.Salary

/**
 * Created by Derek on 11/06/2018
 */
@Entity(tableName = "accounts")
data class DbAccount(
  @PrimaryKey val crewCode: String = "",
  val name: String = "",
  val companyId: Int,
  val crewType: String = "",
  val base: String = "",
  val joinedCompanyAt: Long,
  val updateFlightsRealTimeEnabled: Boolean = false,
  @Embedded val salary: Salary = Salary(),
  @Embedded val futureDaysPattern: FutureDaysPattern = FutureDaysPattern()
)