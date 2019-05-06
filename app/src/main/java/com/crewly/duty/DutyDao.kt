package com.crewly.duty

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Derek on 14/06/2018
 */
@Dao
interface DutyDao {

  @Query("SELECT * FROM duties")
  fun observeAllDuties(): Flowable<List<Duty>>

  @Query("SELECT * FROM duties WHERE date >= :startTime AND date <= :endTime")
  fun observeDutiesBetween(startTime: Long, endTime: Long): Flowable<List<Duty>>

  @Query("SELECT * FROM duties WHERE crew_code is :crewCode AND date >= :startTime AND date <= :endTime")
  fun fetchDutiesBetween(crewCode: String, startTime: Long, endTime: Long): Single<List<Duty>>

  @Query("DELETE FROM duties")
  fun deleteAllDuties()

  @Query("DELETE FROM duties WHERE date >= :time")
  fun deleteAllDutiesFrom(time: Long): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertDuties(duties: List<Duty>): Completable

  @Update
  fun updateDuties(duties: List<Duty>)
}