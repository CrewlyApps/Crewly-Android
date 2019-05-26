package com.crewly.db.duty

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
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

  var type: String = "",
  var date: DateTime = DateTime(),
  var location: String = "",
  var description: String = "",

  @ColumnInfo(name = "special_event_type")
  var specialEventType: String = ""
)