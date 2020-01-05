package com.crewly.persistence.crew

import androidx.room.Entity

/**
 * Created by Derek on 02/06/2018
 */
@Entity(
  tableName = "crew",
  primaryKeys = [
    "id",
    "companyId"
  ]
)
data class DbCrew(
  val id: String = "",
  val name: String = "",
  val companyId: Int,
  val rank: String
)