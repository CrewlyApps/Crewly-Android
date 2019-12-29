package com.crewly.roster

import com.crewly.persistence.duty.DbDuty
import com.crewly.persistence.sector.DbSector
import com.crewly.models.Company
import com.crewly.models.DateTimePeriod
import com.crewly.models.Rank
import com.crewly.models.duty.Duty
import com.crewly.models.duty.DutyType
import com.crewly.models.roster.Roster
import com.crewly.models.roster.RosterPeriod
import com.crewly.models.sector.Sector
import com.crewly.network.roster.NetworkCrew
import com.crewly.network.roster.NetworkEvent
import com.crewly.network.roster.NetworkFlight
import com.crewly.persistence.crew.DbCrew
import com.crewly.repositories.CrewRepository
import com.crewly.repositories.DutiesRepository
import com.crewly.repositories.RosterNetworkRepository
import com.crewly.repositories.SectorsRepository
import com.crewly.utils.withTimeAtEndOfDay
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import javax.inject.Inject

/**
 * Created by Derek on 02/06/2018
 */
class RosterRepository @Inject constructor(
  private val crewRepository: CrewRepository,
  private val dutiesRepository: DutiesRepository,
  private val rosterNetworkRepository: RosterNetworkRepository,
  private val sectorsRepository: SectorsRepository
) {

  private val dateTimeParser by lazy { ISODateTimeFormat.dateTimeParser() }

  fun fetchRoster(
    username: String,
    password: String,
    companyId: Int
  ): Completable =
    rosterNetworkRepository.fetchRoster(
      username = username,
      password = password,
      companyId = companyId
    )
      .map { roster ->
        val allDuties = mutableListOf<DbDuty>()
        val allSectors = mutableListOf<DbSector>()
        val uniqueCrew = mutableSetOf<NetworkCrew>()

        roster.days.forEach { (_, events, flights, crew) ->
          val duties = events.map { event ->
            event.toDbDuty(
              ownerId = username,
              companyId = companyId
            )
          }

          val sectors = flights.map { flight ->
            flight.toDbSector(
              ownerId = username,
              companyId = companyId,
              crew = crew.map { it.fullName }
            )
          }

          allDuties.addAll(duties)
          allSectors.addAll(sectors)
          uniqueCrew.addAll(crew)
        }

        val allCrew = uniqueCrew.map { crew ->
          crew.toDbCrew(
            companyId = companyId
          )
        }

        Triple(allDuties, allSectors, allCrew)
      }
      .flatMapCompletable { (allDuties, allSectors, allCrew) ->
        Completable.mergeArray(
          dutiesRepository.saveDuties(allDuties),
          sectorsRepository.saveSectors(allSectors),
          crewRepository.saveCrew(allCrew)
        )
      }

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

    return dutiesRepository
      .getDutiesBetween(
        ownerId = crewCode,
        startTime = month.millis,
        endTime = nextMonth.millis
      )
      .map { dbDuties ->
        dbDuties.map { it.toDuty() }
      }
      .zipWith(
        sectorsRepository
          .getSectorsBetween(
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

    return dutiesRepository
      .getDutiesBetween(
        ownerId = crewCode,
        startTime = firstDay.millis,
        endTime = lastDay.millis
      )
      .map { dbDuties ->
        dbDuties.map { it.toDuty() }
      }
      .zipWith(
        sectorsRepository
        .getSectorsBetween(
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
    return dutiesRepository
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
    sectorsRepository
      .getSectorsBetween(
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
    return sectorsRepository
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
      dutiesRepository
        .saveDuties(
          duties = roster.duties.map { it.toDbDuty() }
        ),
      sectorsRepository
        .saveSectors(
          sectors = roster.sectors.map { it.toDbSector() }
        )
    )

  fun deleteRosterFromDay(
    crewCode: String,
    day: DateTime
  ): Completable {
    val startOfDay = day.withTimeAtStartOfDay()
    return dutiesRepository.deleteAllDutiesFrom(
      ownerId = crewCode,
      from = startOfDay.millis
    )
      .mergeWith(
        sectorsRepository.deleteAllSectorsFrom(
          ownerId = crewCode,
          from = startOfDay.millis
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
    var dutiesPerDay = mutableListOf<Duty>()
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

      dutiesPerDay.add(it)

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
    duties: MutableList<Duty>
  ): RosterPeriod.RosterDate {
    val firstDuty = duties[0]
    return RosterPeriod.RosterDate(
      date = firstDuty.startTime,
      duties = duties
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

  private fun NetworkEvent.toDbDuty(
    ownerId: String,
    companyId: Int
  ): DbDuty =
    DbDuty(
      ownerId = ownerId,
      companyId = companyId,
      type = type,
      code = code,
      startTime = dateTimeParser.parseDateTime(time).millis,
      location = location
    )

  private fun NetworkFlight.toDbSector(
    ownerId: String,
    companyId: Int,
    crew: List<String>
  ): DbSector =
    DbSector(
      flightId = code,
      ownerId = ownerId,
      companyId = companyId,
      departureAirport = from,
      arrivalAirport = to,
      departureTime = dateTimeParser.parseDateTime(start).millis,
      arrivalTime = dateTimeParser.parseDateTime(end).millis,
      crew = crew
    )

  private fun NetworkCrew.toDbCrew(
    companyId: Int
  ): DbCrew =
    DbCrew(
      id = fullName,
      name = fullName,
      companyId = companyId,
      rank = Rank.fromName(rank).getValue()
    )

  private fun Duty.toDbDuty(): DbDuty =
    DbDuty(
      id = id,
      ownerId = ownerId,
      companyId = company.id,
      type = type.name,
      code = code,
      startTime = startTime.millis,
      endTime = endTime.millis,
      location = location,
      specialEventType = specialEventType
    )

  private fun DbDuty.toDuty(): Duty =
    Duty(
      id = id,
      ownerId = ownerId,
      company = Company.fromId(companyId),
      type = DutyType(type),
      startTime = DateTime(startTime),
      endTime = DateTime(endTime),
      location = location,
      specialEventType = specialEventType
    )

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