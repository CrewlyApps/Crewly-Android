package com.crewly.repositories

import com.crewly.persistence.duty.DbDuty
import com.crewly.persistence.flight.DbFlight
import com.crewly.models.DateTimePeriod
import com.crewly.models.account.CrewType
import com.crewly.models.duty.Duty
import com.crewly.models.duty.DutyType
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

  companion object {
    private const val CREW_CONSECUTIVE_DAYS_ON = 5
    private const val CREW_CONSECUTIVE_DAYS_OFF = 3

    private const val PILOT_CONSECUTIVE_DAYS_ON = 6
    private const val PILOT_CONSECUTIVE_DAYS_OFF = 4
  }

  data class FetchRosterData(
    val userBase: String
  )

  private data class SaveRosterData(
    val roster: NetworkRoster,
    val duties: List<DbDuty>,
    val flights: List<DbFlight>,
    val crew: List<DbCrew>,
    val rosterData: FileData
  )

  private val dateTimeParser by lazy { ISODateTimeFormat.dateTimeParser() }
  private val dateTimeFormatter by lazy { ISODateTimeFormat.dateTime() }

  fun fetchRoster(
    username: String,
    password: String,
    companyId: Int,
    crewType: CrewType
  ): Single<FetchRosterData> =
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
          companyId = companyId,
          crewType = crewType
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
        if (status == "cancelled") {
          throw Exception(it.reason)
        }

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
    companyId: Int,
    crewType: CrewType
  ): Single<FetchRosterData> =
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
          .map {
            val futureDays = generateFutureRosterDays(
              crewType = crewType,
              rosterDays = roster.days
            )

            roster.copy(
              days = roster.days.plus(futureDays)
            ) to it
          }
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
      .flatMap { (roster, allDuties, allFlights, allCrew, rosterData) ->
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
          .toSingle {
            FetchRosterData(
              userBase = roster.base
            )
          }
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

    val dutiesByStartTime = duties.groupBy { it.startTime.dayOfMonth().get() }
    val flightsByDepartureTime = flights.groupBy { it.departureTime.dayOfMonth().get() }
    val allDays = dutiesByStartTime.keys.plus(flightsByDepartureTime.keys).toSortedSet()

    allDays.forEach { day ->
      val dutiesForDay = dutiesByStartTime[day] ?: emptyList()
      val flightsForDay = flightsByDepartureTime[day] ?: emptyList()
      val date = dutiesForDay.firstOrNull()?.startTime ?: flightsForDay.firstOrNull()?.departureTime

      if (date != null) {
        rosterDates.add(
          RosterPeriod.RosterDate(
            date = date,
            duties = dutiesForDay,
            flights = flightsForDay.toMutableList()
          )
        )
      }
    }

    return rosterDates
  }

  private fun generateFutureRosterDays(
    crewType: CrewType,
    rosterDays: List<NetworkRosterDay>
  ): List<NetworkRosterDay> {
    val futureRosterDays = mutableListOf<NetworkRosterDay>()
    val daysOn = if (crewType == CrewType.FLIGHT) PILOT_CONSECUTIVE_DAYS_ON else CREW_CONSECUTIVE_DAYS_ON
    val daysOff = if (crewType == CrewType.FLIGHT) PILOT_CONSECUTIVE_DAYS_OFF else CREW_CONSECUTIVE_DAYS_OFF
    val numberOfRosterDays = rosterDays.size
    val lastRosterDay = rosterDays.last()
    val lastRosterDate = dateTimeParser.parseDateTime(lastRosterDay.date)
    val monthEndDate = lastRosterDate.dayOfMonth().withMaximumValue()
    val lastDate = 365 + (monthEndDate.dayOfMonth - lastRosterDate.dayOfMonth) + 1
    var daysOnCount = 0
    var daysOffCount = 0

    val isLastDayOffDay = lastRosterDay.events.find { event -> event.isOffDay() } != null

    if (isLastDayOffDay) {
      loop@ for (i in 1 until daysOff) {
        val rosterDay = rosterDays[numberOfRosterDays - i]
        val isDayOff = rosterDay.events.find { event -> event.isOffDay() } != null
        if (!isDayOff) {
          daysOffCount = i
          break@loop
        }
      }

      if (daysOffCount >= daysOff) {
        daysOnCount = 0
        daysOffCount = 0
      }

    } else {
      loop@ for (i in 1 until daysOn) {
        val rosterDay = rosterDays[numberOfRosterDays - i]
        val isDayOff = rosterDay.events.find { event -> event.isOffDay() } != null
        if (isDayOff) {
          daysOnCount = i
          break@loop
        }
      }
    }

    for (i in 1 until lastDate) {
      val eventType = if (daysOnCount < daysOn) {
        daysOnCount++
        DutyType.UNKNOWN
      } else {
        if (++daysOffCount >= daysOff) {
          daysOnCount = 0
          daysOffCount = 0
        }

        DutyType.TYPE_OFF
      }

      val offDayEvent = NetworkEvent(
        type = eventType,
        code = "OFF"
      )

      val rosterDay = NetworkRosterDay(
        date = dateTimeFormatter.print(lastRosterDate.plusDays(i)),
        events = listOf(offDayEvent)
      )

      futureRosterDays.add(rosterDay)
    }

    return futureRosterDays
  }

  private fun NetworkEvent.isOffDay(): Boolean =
    DutyType(
      name = type,
      code = code
    ).isOff()

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
      from = if (from.isNotBlank()) from else location,
      to = to,
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