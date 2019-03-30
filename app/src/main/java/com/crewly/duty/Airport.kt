package com.crewly.duty

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Created by Derek on 22/07/2018
 */
@Entity(tableName = "airports")
data class Airport(
  @PrimaryKey var code_iata: String = "",
  var code_icao: String = "",
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