package com.crewly.account

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.crewly.crew.Rank
import org.joda.time.DateTime

/**
 * Created by Derek on 11/06/2018
 */
@Entity(tableName = "accounts")
data class Account(@PrimaryKey
                   @ColumnInfo(name = "crew_code") var crewCode: String = "",

                   var name: String = "",

                   var company: String = "",

                   var base: String = "",

                   var rank: Rank = Rank.NONE,

                   @ColumnInfo(name = "is_pilot")
                   var isPilot: Boolean = false,

                   @ColumnInfo(name = "joined_company_at")
                   var joinedCompanyAt: DateTime = DateTime(0),

                   @ColumnInfo(name = "base_salary")
                   var baseSalary: Float = 0f,

                   @ColumnInfo(name = "salary_per_hour")
                   var salaryPerHour: Float = 0f,

                   @ColumnInfo(name = "show_crew")
                   var showCrew: Boolean = false,

                   @ColumnInfo(name = "salary_calculation_enabled")
                   var salaryCalculationEnabled: Boolean = false,

                   @ColumnInfo(name = "salary_per_hour_rostered_enabled")
                   var salaryPerHourRosteredEnabled: Boolean = false,

                   @ColumnInfo(name = "update_sectors_real_time_enabled")
                   var updateSectorsRealTimeEnabled: Boolean = false) {

    @Ignore constructor(): this("")
}