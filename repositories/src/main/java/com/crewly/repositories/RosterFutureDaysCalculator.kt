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

  private class DaysCounter(
    var daysOnCount: Int = 0,
    var daysOffCount: Int = 0,
    var secondDaysOnCount: Int = 0,
    var secondDaysOffCount: Int = 0
  )

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

        val daysCounter = if (pattern.areFirstAndSecondPatternsDifferent()) {
          countDaysForMultiPattern(
            pattern = pattern,
            rosterDays = rosterDays
          )
        } else {
          countDaysForSinglePattern(
            pattern = pattern,
            rosterDays = rosterDays
          )
        }

        if (pattern.areFirstAndSecondPatternsDifferent()) {
          generateFutureDaysForMultiPattern(
            pattern = pattern,
            daysCounter = daysCounter,
            lastRosterDay = rosterDays.last()
          )
        } else {
          generateFutureDaysForSinglePattern(
            pattern = pattern,
            daysCounter = daysCounter,
            lastRosterDay = rosterDays.last()
          )
        }
      }

  private fun countDaysForSinglePattern(
    pattern: FutureDaysPattern,
    rosterDays: List<NetworkRosterDay>
  ): DaysCounter {
    val counter = DaysCounter()
    val numberOfRosterDays = rosterDays.size
    val lastRosterDay = rosterDays.last()
    val isLastDayOffDay = lastRosterDay.events.find { event -> event.isOffDay() } != null

    if (isLastDayOffDay) {
      val daysOffCount = countNumberOfConsecutiveDaysOff(
        rosterDays = rosterDays,
        startIndex = numberOfRosterDays - 1,
        daysOffThreshold = pattern.firstNumberOfDaysOff
      )

      if (daysOffCount >= pattern.firstNumberOfDaysOff) {
        counter.daysOnCount = 0
        counter.daysOffCount = 0
      } else {
        counter.daysOffCount = daysOffCount
      }
    } else {
      val daysOnCount = countNumberOfConsecutiveDaysOn(
        rosterDays = rosterDays,
        startIndex = numberOfRosterDays - 1,
        daysOnThreshold = pattern.firstNumberOfDaysOn
      )

      counter.daysOnCount = daysOnCount
    }

    return counter
  }

  private fun countDaysForMultiPattern(
    pattern: FutureDaysPattern,
    rosterDays: List<NetworkRosterDay>
  ): DaysCounter {
    val counter = DaysCounter()
    val numberOfRosterDays = rosterDays.size
    val lastRosterDay = rosterDays.last()
    val isLastDayOffDay = lastRosterDay.events.find { event -> event.isOffDay() } != null



    return counter
  }

  private fun countNumberOfConsecutiveDaysOff(
    rosterDays: List<NetworkRosterDay>,
    startIndex: Int,
    daysOffThreshold: Int
  ): Int {
    var result = 0
    loop@ for (i in 0 until daysOffThreshold) {
      val rosterIndex = startIndex - i

      // No more days to loop through
      if (rosterIndex < 0) break@loop

      val rosterDay = rosterDays[rosterIndex]
      val isDayOff = rosterDay.events.find { event -> event.isOffDay() } != null
      if (!isDayOff) {
        result = i
        break@loop
      }
    }

    return result + 1
  }

  private fun countNumberOfConsecutiveDaysOn(
    rosterDays: List<NetworkRosterDay>,
    startIndex: Int,
    daysOnThreshold: Int
  ): Int {
    var result = 0
    loop@ for (i in 1 until daysOnThreshold) {
      val rosterIndex = startIndex - i

      // No more days to loop through
      if (rosterIndex < 0) break@loop

      val rosterDay = rosterDays[rosterIndex]
      val isDayOff = rosterDay.events.find { event -> event.isOffDay() } != null
      if (isDayOff) {
        result = i
        break@loop
      }
    }

    return result + 1
  }

  private fun generateFutureDaysForSinglePattern(
    pattern: FutureDaysPattern,
    daysCounter: DaysCounter,
    lastRosterDay: NetworkRosterDay
  ): List<NetworkRosterDay> {
    val futureRosterDays = mutableListOf<NetworkRosterDay>()
    val lastRosterDate = dateTimeParser.parseDateTime(lastRosterDay.date)
    val monthEndDate = lastRosterDate.dayOfMonth().withMaximumValue()
    val lastDate = 365 + (monthEndDate.dayOfMonth - lastRosterDate.dayOfMonth) + 1

    for (i in 1 until lastDate) {
      val eventType = if (daysCounter.daysOnCount < pattern.firstNumberOfDaysOn) {
        daysCounter.daysOnCount++
        DutyType.UNKNOWN
      } else {
        if (++daysCounter.daysOffCount >= pattern.firstNumberOfDaysOff) {
          daysCounter.daysOnCount = 0
          daysCounter.daysOffCount = 0
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

  private fun generateFutureDaysForMultiPattern(
    pattern: FutureDaysPattern,
    daysCounter: DaysCounter,
    lastRosterDay: NetworkRosterDay
  ): List<NetworkRosterDay> {
    val futureRosterDays = mutableListOf<NetworkRosterDay>()
    val lastRosterDate = dateTimeParser.parseDateTime(lastRosterDay.date)
    val monthEndDate = lastRosterDate.dayOfMonth().withMaximumValue()
    val lastDate = 365 + (monthEndDate.dayOfMonth - lastRosterDate.dayOfMonth) + 1

    return futureRosterDays
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