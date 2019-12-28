package com.crewly.persistence.sector

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Derek on 13/06/2018
 */
@Dao
interface SectorDao {

  @Query("SELECT * FROM sectors WHERE ownerId is :ownerId")
  fun observeAllSectors(
    ownerId: String
  ): Flowable<List<DbSector>>

  @Query("SELECT * FROM sectors WHERE ownerId IS :ownerId AND departureTime >= :startTime AND departureTime <= :endTime")
  fun observeSectorsBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Flowable<List<DbSector>>

  @Query("SELECT * FROM sectors WHERE ownerId IS :ownerId AND departureTime >= :startTime AND departureTime <= :endTime")
  fun fetchSectorsBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Single<List<DbSector>>

  @Query("DELETE FROM sectors WHERE ownerId IS :ownerId")
  fun deleteAllSectors(
    ownerId: String
  ): Completable

  @Query("DELETE FROM sectors WHERE ownerId IS :ownerId AND departureTime >= :time")
  fun deleteAllSectorsFrom(
    ownerId: String,
    time: Long
  ): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertSectors(sectors: List<DbSector>): Completable

  @Update
  fun updateSectors(sectors: List<DbSector>): Completable
}