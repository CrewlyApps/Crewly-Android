package com.crewly.repositories

import com.crewly.models.Company
import com.crewly.models.airport.Airport
import com.crewly.models.duty.Duty
import com.crewly.models.duty.DutyType
import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.duty.DbDuty
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import javax.inject.Inject

class DutiesRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase,
  private val airportsRepository: AirportsRepository
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
      .flatMap { duties ->
        airportsRepository.fetchAirportsForDuties(
          duties = duties
        )
          .map { airports ->
            duties to airports
          }
      }
      .map { (dbDuties, airports) ->
        buildDuties(
          dbDuties = dbDuties,
          airports = airports
        )
      }

  fun observeDutiesBetween(
    ownerId: String,
    startTime: Long,
    endTime: Long
  ): Observable<List<Duty>> =
    crewlyDatabase.dutyDao().observeDutiesBetween(
      ownerId = ownerId,
      startTime = startTime,
      endTime = endTime
    )
      .toObservable()
      .flatMap { duties ->
        airportsRepository.fetchAirportsForDuties(
          duties = duties
        )
          .map { airports ->
            duties to airports
          }
          .toObservable()
      }
      .map { (dbDuties, airports) ->
        buildDuties(
          dbDuties = dbDuties,
          airports = airports
        )
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
      .flatMap { duties ->
        airportsRepository.fetchAirportsForDuties(
          duties = duties
        )
          .map { airports ->
            duties to airports
          }
          .toFlowable()
      }
      .map { (dbDuties, airports) ->
        buildDuties(
          dbDuties = dbDuties,
          airports = airports
        )
      }

  private fun buildDuties(
    dbDuties: List<DbDuty>,
    airports: List<Airport>
  ): List<Duty> {
    val mappedAirports = airports.associateBy { it.codeIata }
    return dbDuties.map { dbDuty ->
      dbDuty.toDuty(
        from = mappedAirports.getOrElse(dbDuty.from) { Airport() },
        to = mappedAirports.getOrElse(dbDuty.to) { Airport() }
      )
    }
  }

  private fun DbDuty.toDuty(
    from: Airport,
    to: Airport
  ): Duty =
    Duty(
      id = id,
      ownerId = ownerId,
      company = Company.fromId(companyId),
      type = DutyType(type, code),
      startTime = DateTime(startTime),
      endTime = DateTime(endTime),
      from = from,
      to = to,
      phoneNumber = phoneNumber
    )
}