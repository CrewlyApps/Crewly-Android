package com.crewly.repositories

import com.crewly.models.Company
import com.crewly.models.sector.Sector
import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.sector.DbSector
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.joda.time.DateTime
import javax.inject.Inject

class SectorsRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase
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
      .map { dbSectors ->
        dbSectors.map { it.toSector() }
      }

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
      .map { dbSectors ->
        dbSectors.map { it.toSector() }
      }

  private fun DbSector.toSector(): Sector =
    Sector(
      flightId = flightId,
      arrivalAirport = arrivalAirport,
      departureAirport = departureAirport,
      arrivalTime = DateTime(arrivalTime),
      departureTime = DateTime(departureTime),
      ownerId = ownerId,
      company = Company.fromId(companyId),
      crew = crew.toMutableList()
    )
}