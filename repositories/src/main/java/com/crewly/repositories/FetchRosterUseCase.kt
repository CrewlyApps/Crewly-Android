package com.crewly.repositories

import com.crewly.models.account.CrewType
import com.crewly.models.duty.DutyType
import com.crewly.models.roster.future.EventTypesByDate
import com.crewly.models.roster.future.FutureDay
import com.crewly.network.roster.NetworkCrew
import com.crewly.network.roster.NetworkEvent
import com.crewly.network.roster.NetworkFlight
import com.crewly.network.roster.NetworkRosterDay
import com.crewly.persistence.crew.DbCrew
import com.crewly.persistence.duty.DbDuty
import com.crewly.persistence.flight.DbFlight
import io.reactivex.Single
import org.joda.time.format.ISODateTimeFormat
import javax.inject.Inject

class FetchRosterUseCase @Inject constructor(
  accountRepository: AccountRepository,
  private val rosterRepository: RosterRepository
) {

  private val dateTimeParser by lazy { ISODateTimeFormat.dateTimeParser() }
  private val dateTimeFormatter by lazy { ISODateTimeFormat.dateTime() }

  private val futureDaysCalculator = RosterFutureDaysCalculator(
    accountRepository = accountRepository
  )

  fun fetchRoster(
    username: String,
    password: String,
    companyId: Int,
    crewType: CrewType
  ): Single<RosterRepository.FetchRosterData> =
    rosterRepository.triggerRosterFetch(
      username = username,
      password = password,
      companyId = companyId
    )
      .flatMap { jobId ->
        rosterRepository.confirmPendingNotificationIfNeeded(
          username = username,
          password = password,
          companyId = companyId,
          jobId = jobId
        )
      }
      .flatMapCompletable { jobId ->
        rosterRepository.pollForRosterFetchJobCompletion(
          jobId = jobId
        )
      }
      .andThen(
        rosterRepository.fetchRoster(
          username = username,
          password = password,
          companyId = companyId
        )
      )
      .flatMap { (roster, rosterData) ->
        rosterRepository.readDutiesPriorToRoster(
          username = username,
          rosterDays = roster.days
        )
          .map { eventTypesByDate ->
            Triple(eventTypesByDate, roster, rosterData)
          }
      }
      .flatMap { (savedRoster, roster, rosterData) ->
        futureDaysCalculator.generateFutureRosterDays(
          eventTypesByDate = savedRoster.plus(
            roster.days.map { it.toEventTypesByDate() }),
          crewType = crewType
        )
          .map { futureDays ->
            Triple(roster, futureDays, rosterData)
          }
      }
      .map { (roster, futureDays, rosterData) ->
        val allDuties = mutableListOf<DbDuty>()
        val allFlights = mutableListOf<DbFlight>()
        val uniqueCrew = mutableSetOf<NetworkCrew>()

        val fullRoster = roster.copy(
          days = roster.days.plus(
            futureDays.map { it.toNetworkRosterDay() }
          )
        )

        fullRoster.days.forEach { (date, events, flights, crew) ->
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

        RosterRepository.SaveRosterData(
          roster = roster,
          futureDays = futureDays,
          duties = allDuties,
          flights = allFlights,
          crew = allCrew,
          rosterData = rosterData
        )
      }
      .flatMap { data ->
        rosterRepository.deleteSavedDataFromFirstRosterDay(
          username = username,
          data = data
        )
      }
      .flatMap { data ->
        rosterRepository.saveRoster(
          username = username,
          data = data
        )
          .toSingle {
            RosterRepository.FetchRosterData(
              userBase = data.roster.base
            )
          }
      }

  private fun NetworkRosterDay.toEventTypesByDate() =
    EventTypesByDate(
      date = dateTimeParser.parseDateTime(date),
      events = events.map { it.toDutyType() }
    )

  private fun NetworkEvent.toDutyType() =
    DutyType(
      name = type,
      code = code
    )

  private fun FutureDay.toNetworkRosterDay() =
    NetworkRosterDay(
      date = dateTimeFormatter.print(date),
      events = listOf(
        NetworkEvent(
          type = type.name,
          code = type.code
        )
      )
    )

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
}