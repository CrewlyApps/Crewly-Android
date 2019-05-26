package com.crewly.db.crew

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.crewly.crew.Rank
import com.crewly.models.Company
import org.joda.time.DateTime

/**
 * Created by Derek on 02/06/2018
 */
@Entity(
  tableName = "crew",
  primaryKeys = [
    "id",
    "company"
  ]
)
data class Crew(

  val id: String = "",

  val name: String = "",

  val company: Company = Company.None,

  val base: String = "",

  val rank: Rank = Rank.NONE,

  @ColumnInfo(name = "is_pilot")
  val isPilot: Boolean = false,

  @ColumnInfo(name = "joined_company_at")
  val joinedCompanyAt: DateTime = DateTime(0),

  @ColumnInfo(name = "last_seen_at")
  val lastSeenAt: DateTime = DateTime(0),

  @ColumnInfo(name = "show_crew")
  val showCrew: Boolean = false
)