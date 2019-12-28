package com.crewly.persistence.duty

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Derek on 14/06/2018
 */
@Dao
interface DutyDao {

  @Query("SELECT * FROM duties WHERE ownerId is :ownerId")
  fun observeAllDuties(
    ownerId: String
  ): Flowable<List<DbDuty>>

  @Query("SELECT * FROM duties WHERE ownerId is :ownerId AND startTime >= :startTime AND startTime <= :endTime")
  fun observeDutiesBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Flowable<List<DbDuty>>

  @Query("SELECT * FROM duties WHERE ownerId is :ownerId AND startTime >= :startTime AND startTime <= :endTime")
  fun fetchDutiesBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Single<List<DbDuty>>

  @Query("DELETE FROM duties WHERE ownerId is :ownerId")
  fun deleteAllDuties(
    ownerId: String
  ): Completable

  @Query("DELETE FROM duties WHERE ownerId is :ownerId AND startTime >= :time")
  fun deleteAllDutiesFrom(
    ownerId: String,
    time: Long
  ): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertDuties(duties: List<DbDuty>): Completable

  @Update
  fun updateDuties(duties: List<DbDuty>): Completable
}