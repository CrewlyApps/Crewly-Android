package com.crewly.repositories

import com.crewly.models.Company
import com.crewly.models.airport.Airport
import com.crewly.models.sector.Sector
import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.sector.DbSector
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.joda.time.DateTime
import javax.inject.Inject

class SectorsRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase,
  private val airportsRepository: AirportsRepository
) {

  fun saveSectors(
    sectors: List<DbSector>
  ): Completable =
    crewlyDatabase.sectorDao()
      .insertSectors(sectors)

  fun getSectorsBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Single<List<Sector>> =
    crewlyDatabase.sectorDao()
      .fetchSectorsBetween(
        ownerId = ownerId,
        startTime = startTime,
        endTime = endTime
      )
      .flatMap { sectors ->
        airportsRepository.fetchAirportsForSectors(
          sectors = sectors
        )
          .map { flights ->
            sectors to flights
          }
      }
      .map { (dbSectors, airports) ->
        buildSectors(
          dbSectors = dbSectors,
          airports = airports
        )
      }

  fun deleteSectorsFrom(
    ownerId: String,
    time: Long
  ): Completable =
    crewlyDatabase.sectorDao()
      .deleteAllSectorsFrom(
        ownerId = ownerId,
        time = time
      )

  fun observeSectorsForDay(
    ownerId: String,
    date: DateTime
  ): Flowable<List<Sector>> =
    crewlyDatabase.sectorDao()
      .observeSectorsBetween(
        ownerId = ownerId,
        startTime = date.withTimeAtStartOfDay().millis,
        endTime = date.plusDays(1).withTimeAtStartOfDay().minusMillis(1).millis
      )
      .flatMap { sectors ->
        airportsRepository.fetchAirportsForSectors(
          sectors = sectors
        )
          .map { flights ->
            sectors to flights
          }
          .toFlowable()
      }
      .map { (dbSectors, airports) ->
        buildSectors(
          dbSectors = dbSectors,
          airports = airports
        )
      }

  private fun buildSectors(
    dbSectors: List<DbSector>,
    airports: List<Airport>
  ): List<Sector> {
    val mappedAirports = airports.associateBy { it.codeIata }
    return dbSectors.map { dbSector ->
      dbSector.toSector(
        arrivalAirport = mappedAirports.getOrElse(dbSector.arrivalAirport) { Airport() },
        departureAirport = mappedAirports.getOrElse(dbSector.departureAirport) { Airport() }
      )
    }
  }

  private fun DbSector.toSector(
    arrivalAirport: Airport,
    departureAirport: Airport
  ): Sector =
    Sector(
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