package com.crewly.persistence.flight

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Derek on 13/06/2018
 */
@Dao
interface FlightDao {

  @Query("SELECT * FROM flights WHERE ownerId is :ownerId")
  fun observeAllFlights(
    ownerId: String
  ): Flowable<List<DbFlight>>

  @Query("SELECT * FROM flights WHERE ownerId IS :ownerId AND departureTime >= :startTime AND departureTime <= :endTime")
  fun observeFlightsBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Flowable<List<DbFlight>>

  @Query("SELECT * FROM flights WHERE ownerId IS :ownerId AND departureTime >= :startTime AND departureTime <= :endTime")
  fun fetchFlightsBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Single<List<DbFlight>>

  @Query("DELETE FROM flights WHERE ownerId IS :ownerId")
  fun deleteAllFlights(
    ownerId: String
  ): Completable

  @Query("DELETE FROM flights WHERE ownerId IS :ownerId AND departureTime >= :time")
  fun deleteAllFlightsFrom(
    ownerId: String,
    time: Long
  ): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertFlights(flights: List<DbFlight>): Completable

  @Update
  fun updateFlights(flights: List<DbFlight>): Completable
}