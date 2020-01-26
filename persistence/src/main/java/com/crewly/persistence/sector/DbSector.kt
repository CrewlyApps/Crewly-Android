package com.crewly.persistence.sector

import androidx.room.Entity
import androidx.room.Index

/**
 * Created by Derek on 14/06/2018
 * @param ownerId The id of the user who's roster this sector belongs to.
 * @param crew A list of all crew members for this sector
 */
@Entity(
  tableName = "sectors",
  primaryKeys = [
    "name",
    "companyId",
    "departureTime",
    "departureAirport",
    "arrivalAirport"
  ],
  indices = [(Index("departureTime"))]
)
data class DbSector(
  val name: String = "",
  val code: String,
  val number: String,
  val arrivalAirport: String = "",
  val departureAirport: String = "",
  val arrivalTime: Long,
  val departureTime: Long,
  val ownerId: String = "",
  val companyId: Int,
  val isDeadHeaded: Boolean,
  val crew: List<String>
)