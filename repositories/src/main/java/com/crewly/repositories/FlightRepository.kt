package com.crewly.repositories

import com.crewly.models.Company
import com.crewly.models.airport.Airport
import com.crewly.models.flight.Flight
import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.flight.DbFlight
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import javax.inject.Inject

class FlightRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase,
  private val airportsRepository: AirportsRepository
) {

  fun saveFlights(
    flights: List<DbFlight>
  ): Completable =
    crewlyDatabase.flightDao()
      .insertFlights(flights)

  fun getFlightsBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Single<List<Flight>> =
    crewlyDatabase.flightDao()
      .fetchFlightsBetween(
        ownerId = ownerId,
        startTime = startTime,
        endTime = endTime
      )
      .flatMap { flights ->
        airportsRepository.fetchAirportsForFlights(
          flights = flights
        )
          .map { airports ->
            flights to airports
          }
      }
      .map { (dbFlights, airports) ->
        buildFlights(
          dbFlights = dbFlights,
          airports = airports
        )
      }

  fun observeFlightsBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Observable<List<Flight>> =
    crewlyDatabase.flightDao().observeFlightsBetween(
      ownerId = ownerId,
      startTime = startTime,
      endTime = endTime
    )
      .toObservable()
      .flatMap { flights ->
        airportsRepository.fetchAirportsForFlights(
          flights = flights
        )
          .map { airports ->
            flights to airports
          }
          .toObservable()
      }
      .map { (dbFlights, airports) ->
        buildFlights(
          dbFlights = dbFlights,
          airports = airports
        )
      }

  fun deleteFlightsFrom(
    ownerId: String,
    time: Long
  ): Completable =
    crewlyDatabase.flightDao()
      .deleteAllFlightsFrom(
        ownerId = ownerId,
        time = time
      )

  fun observeFlightsForDay(
    ownerId: String,
    date: DateTime
  ): Flowable<List<Flight>> =
    crewlyDatabase.flightDao()
      .observeFlightsBetween(
        ownerId = ownerId,
        startTime = date.withTimeAtStartOfDay().millis,
        endTime = date.plusDays(1).withTimeAtStartOfDay().minusMillis(1).millis
      )
      .flatMap { flights ->
        airportsRepository.fetchAirportsForFlights(
          flights = flights
        )
          .map { airports ->
            flights to airports
          }
          .toFlowable()
      }
      .map { (dbFlights, airports) ->
        buildFlights(
          dbFlights = dbFlights,
          airports = airports
        )
      }

  private fun buildFlights(
    dbFlights: List<DbFlight>,
    airports: List<Airport>
  ): List<Flight> {
    val mappedAirports = airports.associateBy { it.codeIata }
    return dbFlights.map { dbFlight ->
      dbFlight.toFlight(
        arrivalAirport = mappedAirports.getOrElse(dbFlight.arrivalAirport) { Airport() },
        departureAirport = mappedAirports.getOrElse(dbFlight.departureAirport) { Airport() }
      )
    }
  }

  private fun DbFlight.toFlight(
    arrivalAirport: Airport,
    departureAirport: Airport
  ): Flight =
    Flight(
      flightId = name,
      arrivalAirport = arrivalAirport,
      departureAirport = departureAirport,
      arrivalTime = DateTime(arrivalTime),
      departureTime = DateTime(departureTime),
      ownerId = ownerId,
      company = Company.fromId(companyId),
      crew = crew.toMutableList()
    )
}