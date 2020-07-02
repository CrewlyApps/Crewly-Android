package com.crewly.repositories

import com.crewly.models.FutureDaysPattern
import com.crewly.models.account.CrewType
import com.crewly.models.duty.DutyType
import com.crewly.network.roster.NetworkEvent
import com.crewly.network.roster.NetworkRosterDay
import io.reactivex.Single
import org.joda.time.format.ISODateTimeFormat

internal class RosterFutureDaysCalculator(
  private val accountRepository: AccountRepository
) {

  private val dateTimeParser by lazy { ISODateTimeFormat.dateTimeParser() }
  private val dateTimeFormatter by lazy { ISODateTimeFormat.dateTime() }

  fun generateFutureRosterDays(
    crewType: CrewType,
    rosterDays: List<NetworkRosterDay>
  ): Single<List<NetworkRosterDay>> =
    accountRepository.getCurrentAccount()
      .map { account ->
        val pattern = getFutureDaysPattern(
          accountPattern = account.futureDaysPattern,
          crewType = crewType
        )

        val futureRosterDays = mutableListOf<NetworkRosterDay>()
        val numberOfRosterDays = rosterDays.size
        val lastRosterDay = rosterDays.last()
        val lastRosterDate = dateTimeParser.parseDateTime(lastRosterDay.date)
        val monthEndDate = lastRosterDate.dayOfMonth().withMaximumValue()
        val lastDate = 365 + (monthEndDate.dayOfMonth - lastRosterDate.dayOfMonth) + 1
        var daysOnCount = 0
        var daysOffCount = 0

        val isLastDayOffDay = lastRosterDay.events.find { event -> event.isOffDay() } != null

        if (isLastDayOffDay) {
          loop@ for (i in 1 until pattern.firstNumberOfDaysOff) {
            val rosterDay = rosterDays[numberOfRosterDays - i]
            val isDayOff = rosterDay.events.find { event -> event.isOffDay() } != null
            if (!isDayOff) {
              daysOffCount = i
              break@loop
            }
          }

          if (daysOffCount >= pattern.firstNumberOfDaysOff) {
            daysOnCount = 0
            daysOffCount = 0
          }

        } else {
          loop@ for (i in 1 until pattern.firstNumberOfDaysOn) {
            val rosterDay = rosterDays[numberOfRosterDays - i]
            val isDayOff = rosterDay.events.find { event -> event.isOffDay() } != null
            if (isDayOff) {
              daysOnCount = i
              break@loop
            }
          }
        }

        for (i in 1 until lastDate) {
          val eventType = if (daysOnCount < pattern.firstNumberOfDaysOn) {
            daysOnCount++
            DutyType.UNKNOWN
          } else {
            if (++daysOffCount >= pattern.firstNumberOfDaysOff) {
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

        futureRosterDays
      }

  private fun getFutureDaysPattern(
    accountPattern: FutureDaysPattern,
    crewType: CrewType
  ): FutureDaysPattern =
    if (accountPattern.hasPattern()) {
      accountPattern
    } else {
      when (crewType) {
        CrewType.CABIN -> accountPattern.toCrewPattern()
        CrewType.FLIGHT -> accountPattern.toPilotPattern()
      }
    }

  private fun NetworkEvent.isOffDay(): Boolean =
    DutyType(
      name = type,
      code = code
    ).isOff()
}