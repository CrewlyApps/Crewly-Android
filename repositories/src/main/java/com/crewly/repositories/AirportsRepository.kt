package com.crewly.repositories

import android.content.Context
import com.crewly.models.airport.Airport
import com.crewly.models.flight.Flight
import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.airport.DbAirport
import com.crewly.persistence.duty.DbDuty
import com.crewly.persistence.flight.DbFlight
import com.crewly.utils.readAssetsFile
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import javax.inject.Inject

/**
 * Created by Derek on 24/07/2018
 */
class AirportsRepository @Inject constructor(
  private val context: Context,
  private val crewlyDatabase: CrewlyDatabase,
  private val moshi: Moshi
) {

  fun copyAirportsToDatabase(): Completable =
    context.readAssetsFile("airports.json")
      .subscribeOn(Schedulers.io())
      .observeOn(Schedulers.io())
      .map { json ->
        val jsonArray = JSONArray(json)
        val arrayLength = jsonArray.length()
        val airports = mutableListOf<DbAirport>()

        for (i in 0 until arrayLength) {
          val airportJson = jsonArray[i].toString()
          val airport = moshi.adapter(DbAirport::class.java).fromJson(airportJson) ?: DbAirport()
          airports.add(airport)
        }

        airports
      }
      .flatMapCompletable { airports ->
        crewlyDatabase
          .airportDao()
          .insertAirports(airports)
      }

  fun fetchAirportsForFlights(
    flights: List<DbFlight>
  ): Single<List<Airport>> =
    crewlyDatabase.airportDao()
      .fetchAirports(
        codes = flights.fold(mutableSetOf<String>()) { airportCodes, flight ->
          airportCodes.add(flight.departureAirport)
          airportCodes.add(flight.arrivalAirport)
          airportCodes
        }.toList()
    )
      .map { dbAirports ->
        dbAirports.map { dbAirport ->
          dbAirport.toAirport()
        }
      }

  fun fetchAirportsForDuties(
    duties: List<DbDuty>
  ): Single<List<Airport>> =
    crewlyDatabase.airportDao()
      .fetchAirports(
        codes = duties.fold(mutableSetOf<String>()) { airportCodes, duty ->
          airportCodes.add(duty.from)
          airportCodes.add(duty.to)
          airportCodes
        }.toList()
      )
      .map { dbAirports ->
        dbAirports.map { dbAirport ->
          dbAirport.toAirport()
        }
      }

  fun fetchDepartureAirportForFlight(
    flight: Flight
  ): Single<Airport> =
    crewlyDatabase.airportDao()
      .fetchAirport(flight.departureAirport.codeIata)
      .map { dbAirport -> dbAirport.toAirport() }

  fun fetchArrivalAirportForFlight(
    flight: Flight
  ): Single<Airport> =
    crewlyDatabase.airportDao()
      .fetchAirport(flight.arrivalAirport.codeIata)
      .map { dbAirport -> dbAirport.toAirport() }

  private fun DbAirport.toAirport() =
    Airport(
      codeIata = codeIata,
      codeIcao = codeIcao,
      name = name,
      city = city,
      country = country,
      timezone = timezone,
      latitude = latitude,
      longitude = longitude
    )
}