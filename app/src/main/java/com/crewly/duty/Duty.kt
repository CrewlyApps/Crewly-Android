package com.crewly.duty

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import org.joda.time.DateTime

/**
 * Created by Derek on 30/05/2018
 * Represents a duty a in user's roster.
 * @param crewCode The id of the user this duty belongs to
 */
@Entity(tableName = "duties")
data class Duty(
  @PrimaryKey(autoGenerate = true)
  var id: Long = 0,

  @ColumnInfo(name = "crew_code")
  var crewCode: String = "",

  var type: String = "",
  var date: DateTime = DateTime(),
  var location: String = "",
  var description: String = "",

  @ColumnInfo(name = "special_event_type")
  var specialEventType: String = ""
) {

  @Ignore
  constructor(): this(0)
}