package com.crewly.duty

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Single

/**
 * Created by Derek on 22/07/2018
 */
@Dao
interface AirportDao {

    @Query("SELECT * FROM AIRPORTS WHERE code_iata IS :code")
    fun fetchAirport(code: String): Single<Airport>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAirport(airport: Airport)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAirports(airports: List<Airport>)
}