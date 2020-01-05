package com.crewly.repositories

import com.crewly.persistence.duty.DbDuty
import com.crewly.persistence.sector.DbSector
import com.crewly.models.DateTimePeriod
import com.crewly.models.duty.Duty
import com.crewly.models.roster.RosterPeriod
import com.crewly.models.sector.Sector
import com.crewly.network.roster.NetworkCrew
import com.crewly.network.roster.NetworkEvent
import com.crewly.network.roster.NetworkFlight
import com.crewly.persistence.crew.DbCrew
import com.crewly.utils.withTimeAtEndOfDay
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.lang.Exception
import java.util.concurrent.TimeUnit
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
    triggerRosterFetch(
      username = username,
      password = password,
      companyId = companyId
    )
      .flatMap { jobId ->
        confirmPendingNotificationIfNeeded(
          username = username,
          password = password,
          companyId = companyId,
          jobId = jobId
        )
      }
      .flatMapCompletable { jobId ->
        pollForRosterFetchJobCompletion(
          jobId = jobId
        )
      }
      .andThen(
        fetchAndSaveRoster(
          username = username,
          password = password,
          companyId = companyId
        )
      )

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
      .zipWith(
        sectorsRepository
          .getSectorsBetween(
            ownerId = crewCode,
            startTime = month.millis,
            endTime = nextMonth.millis
          ),
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
      .zipWith(
        sectorsRepository
        .getSectorsBetween(
          ownerId = crewCode,
          startTime = firstDay.millis,
          endTime = lastDay.millis
        ),
        BiFunction<List<Duty>, List<Sector>, List<RosterPeriod.RosterDate>> { duties, sectors ->
          combineDutiesAndSectorsToRosterDates(duties, sectors)
        })
  }

  private fun triggerRosterFetch(
    username: String,
    password: String,
    companyId: Int
  ): Single<String> =
    rosterNetworkRepository.triggerRosterFetch(
      username = username,
      password = password,
      companyId = companyId
    )

  /**
   * Check if there is a pending notification that needs to be confirmed before roster can be
   * fetched. If a pending notification is confirmed, the roster fetch trigger will be attempted
   * again.
   *
   * Returns a job id for the roster fetch
   */
  private fun confirmPendingNotificationIfNeeded(
    username: String,
    password: String,
    companyId: Int,
    jobId: String
  ): Single<String> =
    if (jobId.isNotBlank()) {
      Single.just(jobId)
    } else {
      rosterNetworkRepository.confirmPendingNotification(
        username = username,
        password = password,
        companyId = companyId
      )
        .andThen(
          triggerRosterFetch(
            username = username,
            password = password,
            companyId = companyId
          )
        )
    }

  /**
   * Poll and retry for a roster fetch job status until it reports back task completion.
   */
  private fun pollForRosterFetchJobCompletion(
    jobId: String
  ): Completable =
    Observable
      .timer(10, TimeUnit.SECONDS)
      .startWith(0)
      .flatMap {
        rosterNetworkRepository.checkJobStatus(
          jobId = jobId
        )
          .toObservable()
      }
      .doOnNext {
        val status = it.status
        if (status != "completed" || status != "pending") {
          throw Exception(status)
        }
      }
      .takeUntil {
        it.status == "completed"
      }
      .ignoreElements()

  private fun fetchAndSaveRoster(
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

        roster.days.forEach { (date, events, flights, crew) ->
          val duties = events
            .filter { event -> event.code.isNotBlank() }
            .map { event ->
              event.toDbDuty(
                ownerId = username,
                companyId = companyId,
                eventDate = date
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
    companyId: Int,
    eventDate: String
  ): DbDuty {
    val startTime = dateTimeParser.parseDateTime(
      when {
        start.isNotBlank() -> start
        time.isNotBlank() -> time
        else -> eventDate
      }
    ).millis

    val endTime = dateTimeParser.parseDateTime(
      when {
        end.isNotBlank() -> end
        time.isNotBlank() -> time
        else -> eventDate
      }
    ).millis

    return DbDuty(
      ownerId = ownerId,
      companyId = companyId,
      type = type,
      code = code,
      startTime = startTime,
      endTime = endTime,
      location = location,
      phoneNumber = phoneNumber
    )
  }


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
      rank = rank
    )
}