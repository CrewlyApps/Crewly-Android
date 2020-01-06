package com.crewly.persistence.roster

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface RawRosterDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertRawRoster(rawRoster: DbRawRoster): Completable

  @Query("SELECT * FROM rawRosters WHERE ownerId is :ownerId")
  fun getRawRoster(ownerId: String): Single<DbRawRoster>
}