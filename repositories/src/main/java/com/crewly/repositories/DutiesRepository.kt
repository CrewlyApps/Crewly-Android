package com.crewly.repositories

import com.crewly.models.Company
import com.crewly.models.duty.Duty
import com.crewly.models.duty.DutyType
import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.duty.DbDuty
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.joda.time.DateTime
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
  ): Single<List<Duty>> =
    crewlyDatabase.dutyDao()
      .fetchDutiesBetween(
        ownerId = ownerId,
        startTime = startTime,
        endTime = endTime
      )
      .map { dbDuties ->
        dbDuties.map { it.toDuty() }
      }

  fun deleteDutiesFrom(
    ownerId: String,
    time: Long
  ): Completable =
    crewlyDatabase.dutyDao()
      .deleteAllDutiesFrom(
        ownerId = ownerId,
        time = time
      )

  fun observeDutiesForDay(
    ownerId: String,
    date: DateTime
  ): Flowable<List<Duty>> =
    crewlyDatabase.dutyDao()
      .observeDutiesBetween(
        ownerId = ownerId,
        startTime = date.withTimeAtStartOfDay().millis,
        endTime = date.plusDays(1).withTimeAtStartOfDay().minusMillis(1).millis
      )
      .map { dbDuties ->
        dbDuties.map { it.toDuty() }
      }

  private fun DbDuty.toDuty(): Duty =
    Duty(
      id = id,
      ownerId = ownerId,
      company = Company.fromId(companyId),
      type = DutyType(type),
      startTime = DateTime(startTime),
      endTime = DateTime(endTime),
      location = location,
      phoneNumber = phoneNumber
    )
}