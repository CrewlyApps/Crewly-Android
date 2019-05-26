package com.crewly.db.sector

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Derek on 13/06/2018
 */
@Dao
interface SectorDao {

  @Query("SELECT * FROM sectors WHERE owner_id is :ownerId")
  fun observeAllSectors(
    ownerId: String
  ): Flowable<List<Sector>>

  @Query("SELECT * FROM sectors WHERE owner_id IS :ownerId AND departure_time >= :startTime AND departure_time <= :endTime")
  fun observeSectorsBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Flowable<List<Sector>>

  @Query("SELECT * FROM sectors WHERE owner_id IS :ownerId AND departure_time >= :startTime AND departure_time <= :endTime")
  fun fetchSectorsBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Single<List<Sector>>

  @Query("DELETE FROM sectors WHERE owner_id IS :ownerId")
  fun deleteAllSectors(
    ownerId: String
  ): Completable

  @Query("DELETE FROM sectors WHERE owner_id IS :ownerId AND departure_time >= :time")
  fun deleteAllSectorsFrom(
    ownerId: String,
    time: Long
  ): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertSectors(sectors: List<Sector>): Completable

  @Update
  fun updateSectors(sectors: List<Sector>): Completable
}