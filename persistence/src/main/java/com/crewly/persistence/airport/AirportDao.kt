package com.crewly.persistence.airport

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crewly.persistence.airport.Airport
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by Derek on 22/07/2018
 */
@Dao
interface AirportDao {

  @Query("SELECT * FROM AIRPORTS WHERE code_iata IS :code")
  fun fetchAirport(code: String): Single<Airport>

  @Query("SELECT * FROM AIRPORTS WHERE code_iata IN (:codes)")
  fun fetchAirports(codes: List<String>): Single<List<Airport>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertAirport(airport: Airport): Completable

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertAirports(airports: List<Airport>): Completable
}