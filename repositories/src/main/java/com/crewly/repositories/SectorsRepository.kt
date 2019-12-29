package com.crewly.repositories

import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.sector.DbSector
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
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
  ): Single<List<DbSector>> =
    crewlyDatabase.sectorDao()
      .fetchSectorsBetween(
        ownerId = ownerId,
        startTime = startTime,
        endTime = endTime
      )

  fun observeSectorsBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Flowable<List<DbSector>> =
    crewlyDatabase.sectorDao()
      .observeSectorsBetween(
        ownerId = ownerId,
        startTime = startTime,
        endTime = endTime
      )

  fun deleteAllSectorsFrom(
    ownerId: String,
    from: Long
  ): Completable =
    crewlyDatabase.sectorDao()
      .deleteAllSectorsFrom(
        ownerId = ownerId,
        time = from
      )
}