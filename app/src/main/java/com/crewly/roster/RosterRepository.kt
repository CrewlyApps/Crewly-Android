package com.crewly.roster

import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.duty.DbDuty
import com.crewly.persistence.sector.DbSector
import com.crewly.duty.ryanair.RyanairDutyIcon
import com.crewly.duty.ryanair.RyanairDutyType
import com.crewly.models.Company
import com.crewly.models.DateTimePeriod
import com.crewly.models.duty.Duty
import com.crewly.models.duty.FullDuty
import com.crewly.models.roster.Roster
import com.crewly.models.roster.RosterPeriod
import com.crewly.models.sector.Sector
import com.crewly.utils.withTimeAtEndOfDay
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * Created by Derek on 02/06/2018
 */
class RosterRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase
) {

  /**
   * Loads a particular [RosterPeriod.RosterMonth].
   *
   * @param month The month to load. Will use the current time set on the month and fetch one
   * month's worth of data from that time.
   */
  fun fetchRosterMonth(
    crewCode: String,
    month: DateTime
  ): Single<RosterPeriod.RosterMonth> {
    val nextMonth = month.plusMonths(1).minusHours(1)

    return crewlyDatabase.dutyDao()
      .fetchDutiesBetween(
        ownerId = crewCode,
        startTime = month.millis,
        endTime = nextMonth.millis
      )
      .map { dbDuties ->
        dbDuties.map { it.toDuty() }
      }
      .zipWith(
        crewlyDatabase.sectorDao()
          .fetchSectorsBetween(
            ownerId = crewCode,
            startTime = month.millis,
            endTime = nextMonth.millis
          )
          .map { dbSectors ->
            dbSectors.map { it.toSector() }
          },
        BiFunction<List<Duty>, List<Sector>, RosterPeriod.RosterMonth> { duties, sectors ->
          val rosterMonth = RosterPeriod.RosterMonth()
          rosterMonth.rosterDates = combineDutiesAndSectorsToRosterDates(duties, sectors)
          rosterMonth
        })
  }

  /**
   * Loads all [RosterPeriod.RosterDate] for the given [dateTimePeriod]
   */
  fun fetchRosterDays(
    crewCode: String,
    dateTimePeriod: DateTimePeriod
  ): Single<List<RosterPeriod.RosterDate>> {
    val firstDay = dateTimePeriod.startDateTime.withTimeAtStartOfDay()
    val lastDay = dateTimePeriod.endDateTime.withTimeAtEndOfDay()

    return crewlyDatabase.dutyDao()
      .fetchDutiesBetween(
        ownerId = crewCode,
        startTime = firstDay.millis,
        endTime = lastDay.millis
      )
      .map { dbDuties ->
        dbDuties.map { it.toDuty() }
      }
      .zipWith(
        crewlyDatabase.sectorDao()
        .fetchSectorsBetween(
          ownerId = crewCode,
          startTime = firstDay.millis,
          endTime = lastDay.millis
        )
          .map { dbSectors ->
            dbSectors.map { it.toSector() }
          },
        BiFunction<List<Duty>, List<Sector>, List<RosterPeriod.RosterDate>> { duties, sectors ->
          combineDutiesAndSectorsToRosterDates(duties, sectors)
        })
  }

  fun fetchDutiesForDay(
    crewCode: String,
    date: DateTime
  ): Flowable<List<Duty>> {
    val startTime = date.withTimeAtStartOfDay().millis
    val endTime = date.plusDays(1).withTimeAtStartOfDay().minusMillis(1).millis
    return crewlyDatabase
      .dutyDao()
      .observeDutiesBetween(
        ownerId = crewCode,
        startTime = startTime,
        endTime = endTime
      )
      .map { dbDuties ->
        dbDuties.map { it.toDuty() }
      }
  }

  fun fetchSectorsBetween(
    crewCode: String,
    startTime: DateTime,
    endTime: DateTime
  ): Single<List<Sector>> =
    crewlyDatabase
      .sectorDao()
      .fetchSectorsBetween(
        ownerId = crewCode,
        startTime = startTime.millis,
        endTime = endTime.millis
      )
      .map { dbSectors ->
        dbSectors.map { it.toSector() }
      }

  fun fetchSectorsForDay(
    crewCode: String,
    date: DateTime
  ): Flowable<List<Sector>> {
    val startTime = date.withTimeAtStartOfDay().millis
    val endTime = date.plusDays(1).withTimeAtStartOfDay().minusMillis(1).millis
    return crewlyDatabase
      .sectorDao()
      .observeSectorsBetween(
        ownerId = crewCode,
        startTime = startTime,
        endTime = endTime
      )
      .map { dbSectors ->
        dbSectors.map { it.toSector() }
      }
  }

  fun insertOrReplaceRoster(
    roster: Roster
  ): Completable =
    Completable.mergeArray(
      crewlyDatabase.dutyDao()
        .insertDuties(
          duties = roster.duties.map { it.toDbDuty() }
        ),
      crewlyDatabase.sectorDao()
        .insertSectors(
          sectors = roster.sectors.map { it.toDbSector() }
        )
    )

  fun deleteRosterFromDay(
    crewCode: String,
    day: DateTime
  ): Completable {
    val startOfDay = day.withTimeAtStartOfDay()
    return crewlyDatabase.dutyDao().deleteAllDutiesFrom(
      ownerId = crewCode,
      time = startOfDay.millis
    )
      .mergeWith(
        crewlyDatabase.sectorDao().deleteAllSectorsFrom(
          ownerId = crewCode,
          time = startOfDay.millis
        )
      )
  }

  /**
   * Combines a list of [duties] and [sectors] to [RosterPeriod.RosterDate]. All [duties]
   * and [sectors] will be added to the corresponding [RosterPeriod.RosterDate].
   */
  private fun combineDutiesAndSectorsToRosterDates(
    duties: List<Duty>,
    sectors: List<Sector>
  ): MutableList<RosterPeriod.RosterDate> {
    val rosterDates = mutableListOf<RosterPeriod.RosterDate>()

    if (duties.isEmpty()) {
      return rosterDates
    }

    var currentDutyDate = duties.first().startTime
    var dutiesPerDay = mutableListOf<FullDuty>()
    var sectorsAdded = 0

    duties.forEach {
      val dutyDate = it.startTime
      val firstDuty = duties.first() == it
      val lastDuty = duties.last() == it

      if (!firstDuty && currentDutyDate.dayOfMonth() != dutyDate.dayOfMonth()) {
        val rosterDate = createNewRosterDate(dutiesPerDay)
        addSectorsToRosterDate(rosterDate, sectors.drop(sectorsAdded))
        sectorsAdded += rosterDate.sectors.size

        rosterDates.add(rosterDate)
        dutiesPerDay = mutableListOf()
        currentDutyDate = dutyDate
      }

      dutiesPerDay.add(it.toFullDuty())

      // Add the last roster date if it's the last day
      if (lastDuty) {
        val rosterDate = createNewRosterDate(dutiesPerDay)
        addSectorsToRosterDate(rosterDate, sectors.drop(sectorsAdded))
        sectorsAdded += rosterDate.sectors.size
        rosterDates.add(rosterDate)
      }
    }

    return rosterDates
  }

  private fun createNewRosterDate(
    duties: MutableList<FullDuty>
  ): RosterPeriod.RosterDate {
    val firstDuty = duties[0]
    return RosterPeriod.RosterDate(
      date = firstDuty.duty.startTime,
      fullDuties = duties
    )
  }

  private fun addSectorsToRosterDate(
    rosterDate: RosterPeriod.RosterDate,
    remainingSectors: List<Sector>
  ) {
    run {
      remainingSectors.forEach {
        if (rosterDate.date.dayOfMonth == it.departureTime.dayOfMonth) {
          rosterDate.sectors.add(it)
        } else {
          return
        }
      }
    }
  }

  private fun Duty.toDbDuty(): DbDuty =
    DbDuty(
      id = id,
      ownerId = ownerId,
      companyId = company.id,
      type = type,
      startTime = startTime.millis,
      endTime = endTime.millis,
      location = location,
      description = description,
      specialEventType = specialEventType
    )

  private fun DbDuty.toDuty(): Duty =
    Duty(
      id = id,
      ownerId = ownerId,
      company = Company.fromId(companyId),
      type = type,
      startTime = DateTime(startTime),
      endTime = DateTime(endTime),
      location = location,
      description = description,
      specialEventType = specialEventType
    )

  private fun Duty.toFullDuty(): FullDuty =
    when (company) {
      Company.Ryanair -> FullDuty(
        duty = this,
        dutyType = RyanairDutyType(
          name = type
        ),
        dutyIcon = RyanairDutyIcon(
          dutyName = type
        )
      )

      else -> throw Exception("Company ${company.id} not supported")
    }

  private fun Sector.toDbSector(): DbSector =
    DbSector(
      flightId = flightId,
      arrivalAirport = arrivalAirport,
      departureAirport = departureAirport,
      arrivalTime = arrivalTime.millis,
      departureTime = departureTime.millis,
      ownerId = ownerId,
      companyId = company.id,
      crew = crew
    )

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