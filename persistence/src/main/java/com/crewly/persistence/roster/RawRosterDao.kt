package com.crewly.persistence.roster

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single

interface RawRosterDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertRawRoster(rawRoster: DbRawRoster): Completable

  @Query("SELECT * FROM `raw-rosters` WHERE ownerId is :ownerId")
  fun getRawRoster(ownerId: String): Single<DbRawRoster>
}