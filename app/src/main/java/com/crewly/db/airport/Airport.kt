package com.crewly.db.airport

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

/**
 * Created by Derek on 22/07/2018
 */
@Entity(tableName = "airports")
data class Airport(
  @PrimaryKey
  @ColumnInfo(name = "code_iata")
  @field:Json(name = "code_iata")
  @Json(name = "code_iata")
  val codeIata: String = "",

  @ColumnInfo(name = "code_icao")
  @field:Json(name = "code_icao")
  @Json(name = "code_icao")
  val codeIcao: String = "",

  val name: String = "",
  val city: String = "",
  val country: String = "",
  val timezone: String = "",
  val latitude: Float = 0f,
  val longitude: Float = 0f
)