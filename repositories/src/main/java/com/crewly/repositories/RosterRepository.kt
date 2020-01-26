package com.crewly.repositories

import com.crewly.persistence.duty.DbDuty
import com.crewly.persistence.flight.DbFlight
import com.crewly.models.DateTimePeriod
import com.crewly.models.duty.Duty
import com.crewly.models.file.FileData
import com.crewly.models.file.FileFormat
import com.crewly.models.roster.RosterPeriod
import com.crewly.models.flight.Flight
import com.crewly.network.roster.*
import com.crewly.persistence.crew.DbCrew
import com.crewly.persistence.roster.DbRawRoster
import com.crewly.utils.withTimeAtEndOfDay
import io.reactivex.Completable
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
  private val rawRosterRepository: RawRosterRepository,
  private val rosterNetworkRepository: RosterNetworkRepository,
  private val flightRepository: FlightRepository
) {

  private data class SaveRosterData(
    val roster: NetworkRoster,
    val duties: List<DbDuty>,
    val flights: List<DbFlight>,
    val crew: List<DbCrew>,
    val rosterData: FileData
  )

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
  fun getRosterMonth(
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
        flightRepository
          .getFlightsBetween(
            ownerId = crewCode,
            startTime = month.millis,
            endTime = nextMonth.millis
          ),
        BiFunction<List<Duty>, List<Flight>, RosterPeriod.RosterMonth> { duties, flights ->
          val rosterMonth = RosterPeriod.RosterMonth()
          rosterMonth.rosterDates = combineDutiesAndFlightsToRosterDates(duties, flights)
          rosterMonth
        })
  }

  /**
   * Loads all [RosterPeriod.RosterDate] for the given [dateTimePeriod]
   */
  fun getRosterDays(
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
        flightRepository
        .getFlightsBetween(
          ownerId = crewCode,
          startTime = firstDay.millis,
          endTime = lastDay.millis
        ),
        BiFunction<List<Duty>, List<Flight>, List<RosterPeriod.RosterDate>> { duties, flights ->
          combineDutiesAndFlightsToRosterDates(duties, flights)
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
      .flatMap { roster ->
        rosterNetworkRepository.fetchRawRoster(
          username = username,
          fileFormat = FileFormat.fromType(roster.raw.format),
          url = roster.raw.url
        )
          .map { roster to it }
      }
      .map { (roster, rosterData) ->
        val allDuties = mutableListOf<DbDuty>()
        val allFlights = mutableListOf<DbFlight>()
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

          val dbFlights = flights.map { flight ->
            flight.toDbFlight(
              ownerId = username,
              companyId = companyId,
              crew = crew.map { it.fullName }
            )
          }

          allDuties.addAll(duties)
          allFlights.addAll(dbFlights)
          uniqueCrew.addAll(crew)
        }

        val allCrew = uniqueCrew.map { crew ->
          crew.toDbCrew(
            companyId = companyId
          )
        }

        SaveRosterData(
          roster = roster,
          duties = allDuties,
          flights = allFlights,
          crew = allCrew,
          rosterData = rosterData
        )
      }
      .flatMap { data ->
        val firstRosterDay = data.roster.days.firstOrNull()?.date
        val rosterStartTime = firstRosterDay?.let {
          DateTime.parse(it, ISODateTimeFormat.dateTimeParser()).millis
        } ?: 0

        if (rosterStartTime > 0) {
          Completable.mergeArray(
            dutiesRepository.deleteDutiesFrom(
              ownerId = username,
              time = rosterStartTime
            ),
            flightRepository.deleteFlightsFrom(
              ownerId = username,
              time = rosterStartTime
            )
          )
            .toSingle { data }
        } else {
          Single.just(data)
        }
      }
      .flatMapCompletable { (roster, allDuties, allFlights, allCrew, rosterData) ->
        Completable.mergeArray(
          dutiesRepository.saveDuties(allDuties),
          flightRepository.saveFlights(allFlights),
          crewRepository.saveCrew(allCrew),
          rawRosterRepository.saveRawRoster(
            rawRoster = roster.raw.toDbRawRoster(
              username = username,
              rosterData = rosterData
            ),
            rosterData = rosterData
          )
        )
      }

  /**
   * Combines a list of [duties] and [flights] to [RosterPeriod.RosterDate]. All [duties]
   * and [flights] will be added to the corresponding [RosterPeriod.RosterDate].
   */
  private fun combineDutiesAndFlightsToRosterDates(
    duties: List<Duty>,
    flights: List<Flight>
  ): MutableList<RosterPeriod.RosterDate> {
    val rosterDates = mutableListOf<RosterPeriod.RosterDate>()

    if (duties.isEmpty()) {
      return rosterDates
    }

    var currentDutyDate = duties.first().startTime
    var dutiesPerDay = mutableListOf<Duty>()
    var flightsAdded = 0

    duties.forEach {
      val dutyDate = it.startTime
      val firstDuty = duties.first() == it
      val lastDuty = duties.last() == it

      if (!firstDuty && currentDutyDate.dayOfMonth() != dutyDate.dayOfMonth()) {
        val rosterDate = createNewRosterDate(dutiesPerDay)
        addFlightsToRosterDate(rosterDate, flights.drop(flightsAdded))
        flightsAdded += rosterDate.flights.size

        rosterDates.add(rosterDate)
        dutiesPerDay = mutableListOf()
        currentDutyDate = dutyDate
      }

      dutiesPerDay.add(it)

      // Add the last roster date if it's the last day
      if (lastDuty) {
        val rosterDate = createNewRosterDate(dutiesPerDay)
        addFlightsToRosterDate(rosterDate, flights.drop(flightsAdded))
        flightsAdded += rosterDate.flights.size
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

  private fun addFlightsToRosterDate(
    rosterDate: RosterPeriod.RosterDate,
    remainingFlights: List<Flight>
  ) {
    run {
      remainingFlights.forEach {
        if (rosterDate.date.dayOfMonth == it.departureTime.dayOfMonth) {
          rosterDate.flights.add(it)
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

  private fun NetworkFlight.toDbFlight(
    ownerId: String,
    companyId: Int,
    crew: List<String>
  ): DbFlight =
    DbFlight(
      name = if (isDeadHeaded) "DH $number" else number,
      ownerId = ownerId,
      companyId = companyId,
      code = code,
      number = number,
      departureAirport = from,
      arrivalAirport = to,
      departureTime = dateTimeParser.parseDateTime(start).millis,
      arrivalTime = dateTimeParser.parseDateTime(end).millis,
      crew = crew,
      isDeadHeaded = isDeadHeaded
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

  private fun NetworkRawRoster.toDbRawRoster(
    username: String,
    rosterData: FileData
  ): DbRawRoster =
    DbRawRoster(
      ownerId = username,
      fileFormat = format,
      url = url,
      filePath = rosterData.fileName
    )
}