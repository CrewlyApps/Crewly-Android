package com.crewly.repositories

import android.content.Context
import com.crewly.models.airport.Airport
import com.crewly.models.sector.Sector
import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.airport.DbAirport
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

  fun fetchAirportsForSectors(
    sectors: List<Sector>
  ): Single<List<Airport>> =
    crewlyDatabase.airportDao()
      .fetchAirports(
        codes = sectors.fold(mutableSetOf<String>()) { airportCodes, sector ->
          airportCodes.add(sector.departureAirport)
          airportCodes.add(sector.arrivalAirport)
          airportCodes
        }.toList()
    )
      .map { dbAirports ->
        dbAirports.map { dbAirport ->
          dbAirport.toAirport()
        }
      }

  fun fetchDepartureAirportForSector(
    sector: Sector
  ): Single<Airport> =
    crewlyDatabase.airportDao()
      .fetchAirport(sector.departureAirport)
      .map { dbAirport -> dbAirport.toAirport() }

  fun fetchArrivalAirportForSector(
    sector: Sector
  ): Single<Airport> =
    crewlyDatabase.airportDao()
      .fetchAirport(sector.arrivalAirport)
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