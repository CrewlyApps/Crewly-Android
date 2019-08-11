package com.crewly.db.duty

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.crewly.models.Company
import org.joda.time.DateTime

/**
 * Created by Derek on 30/05/2018
 * Represents a duty a in user's roster.
 * @param ownerId The id of the user this duty belongs to
 */
@Entity(tableName = "duties")
data class Duty(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "owner_id")
  var ownerId: String = "",

  var company: Company = Company.None,

  var type: String = "",

  @ColumnInfo(name = "start_time")
  var startTime: DateTime = DateTime(0),

  @ColumnInfo(name = "end_time")
  var endTime: DateTime = DateTime(0),

  var location: String = "",
  var description: String = "",

  @ColumnInfo(name = "special_event_type")
  var specialEventType: String = ""
)