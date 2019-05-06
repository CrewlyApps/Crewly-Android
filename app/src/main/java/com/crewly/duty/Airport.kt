package com.crewly.duty

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

/**
 * Created by Derek on 22/07/2018
 */
@Entity(tableName = "airports")
data class Airport(
  @PrimaryKey
  @ColumnInfo(name = "code_iata")
  @Json(name = "code_iata")
  var codeIata: String = "",

  @ColumnInfo(name = "code_icao")
  @Json(name = "code_icao")
  var codeIcao: String = "",

  var name: String = "",
  var city: String = "",
  var country: String = "",
  var timezone: String = "",
  var latitude: Float = 0f,
  var longitude: Float = 0f
) {

  @Ignore
  constructor(): this("")
}