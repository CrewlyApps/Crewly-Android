package com.crewly.persistence.duty

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Derek on 30/05/2018
 * Represents a duty a in user's roster.
 * @param ownerId The id of the user this duty belongs to
 */
@Entity(tableName = "duties")
data class DbDuty(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val ownerId: String = "",
  val companyId: Int,
  val type: String = "",
  val code: String = "",
  val startTime: Long,
  val endTime: Long = 0,
  val location: String = ""
)