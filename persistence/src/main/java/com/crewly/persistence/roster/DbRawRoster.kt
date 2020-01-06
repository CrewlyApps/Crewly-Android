package com.crewly.persistence.roster

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
  tableName = "rawRosters"
)
data class DbRawRoster(
  @PrimaryKey val ownerId: String,
  val fileFormat: String,
  val url: String,
  val filePath: String
)