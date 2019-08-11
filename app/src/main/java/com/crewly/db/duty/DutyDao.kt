package com.crewly.db.duty

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Derek on 14/06/2018
 */
@Dao
interface DutyDao {

  @Query("SELECT * FROM duties WHERE owner_id is :ownerId")
  fun observeAllDuties(
    ownerId: String
  ): Flowable<List<Duty>>

  @Query("SELECT * FROM duties WHERE owner_id is :ownerId AND start_time >= :startTime AND start_time <= :endTime")
  fun observeDutiesBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Flowable<List<Duty>>

  @Query("SELECT * FROM duties WHERE owner_id is :ownerId AND start_time >= :startTime AND start_time <= :endTime")
  fun fetchDutiesBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Single<List<Duty>>

  @Query("DELETE FROM duties WHERE owner_id is :ownerId")
  fun deleteAllDuties(
    ownerId: String
  ): Completable

  @Query("DELETE FROM duties WHERE owner_id is :ownerId AND start_time >= :time")
  fun deleteAllDutiesFrom(
    ownerId: String,
    time: Long
  ): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertDuties(duties: List<Duty>): Completable

  @Update
  fun updateDuties(duties: List<Duty>): Completable
}