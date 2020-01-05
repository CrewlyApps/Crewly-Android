package com.crewly.persistence.roster

import androidx.room.Entity

@Entity(
  tableName = "raw-rosters"
)
data class DbRawRoster(
  val ownerId: String,
  val fileFormat: String,
  val url: String,
  val filePath: String
)