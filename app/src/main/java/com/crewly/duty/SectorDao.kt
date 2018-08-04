package com.crewly.duty

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Derek on 13/06/2018
 */
@Dao
interface SectorDao {

    @Query("SELECT * FROM sectors")
    fun observeAllSectors(): Flowable<List<Sector>>

    @Query("SELECT * FROM sectors WHERE departure_time >= :startTime AND departure_time <= :endTime")
    fun observeSectorsBetween(startTime: Long, endTime: Long): Flowable<List<Sector>>

    @Query("SELECT * FROM sectors WHERE departure_time >= :startTime AND departure_time <= :endTime")
    fun fetchSectorsBetween2(startTime: Long, endTime: Long): Single<List<Sector>>

    @Query("DELETE FROM sectors")
    fun deleteAllSectors()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSectors(sectors: List<Sector>)

    @Update
    fun updateSectors(sectors: List<Sector>)
}