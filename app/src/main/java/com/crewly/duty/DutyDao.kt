package com.crewly.duty

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Derek on 14/06/2018
 */
@Dao
interface DutyDao {

    @Query("SELECT * FROM duties")
    fun observeAllDuties(): Flowable<List<DutyType>>

    @Query("SELECT * FROM duties WHERE date >= :startTime AND date <= :endTime")
    fun observeDutiesBetween(startTime: Long, endTime: Long): Flowable<List<DutyType>>

    @Query("SELECT * FROM duties WHERE date >= :startTime AND date <= :endTime")
    fun fetchDutiesBetween(startTime: Long, endTime: Long): Single<List<DutyType>>

    @Query("DELETE FROM duties")
    fun deleteAllDuties()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDuties(duties: List<DutyType>)

    @Update
    fun updateDuties(duties: List<DutyType>)
}