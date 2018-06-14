package com.crewly.duty

import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 * Created by Derek on 13/06/2018
 */
@Dao
interface SectorDao {

    @Query("SELECT * FROM sectors")
    fun fetchAllSectors(): Flowable<List<Sector>>

    @Query("SELECT * FROM sectors WHERE departure_time >= :startTime AND departure_time <= :endTime")
    fun fetchSectorsBetween(startTime: Long, endTime: Long): Flowable<List<Sector>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSectors(sectors: List<Sector>)

    @Update
    fun updateSectors(sectors: List<Sector>)
}