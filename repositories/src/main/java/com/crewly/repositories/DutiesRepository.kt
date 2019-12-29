package com.crewly.repositories

import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.duty.DbDuty
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

class DutiesRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase
) {

  fun saveDuties(
    duties: List<DbDuty>
  ): Completable =
    crewlyDatabase.dutyDao()
      .insertDuties(duties)

  fun getDutiesBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Single<List<DbDuty>> =
    crewlyDatabase.dutyDao()
      .fetchDutiesBetween(
        ownerId = ownerId,
        startTime = startTime,
        endTime = endTime
      )

  fun observeDutiesBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Flowable<List<DbDuty>> =
    crewlyDatabase.dutyDao()
      .observeDutiesBetween(
        ownerId = ownerId,
        startTime = startTime,
        endTime = endTime
      )

  fun deleteAllDutiesFrom(
    ownerId: String,
    from: Long
  ): Completable =
    crewlyDatabase.dutyDao()
      .deleteAllDutiesFrom(
        ownerId = ownerId,
        time = from
      )
}