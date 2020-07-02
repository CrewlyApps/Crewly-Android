package com.crewly.repositories

import com.crewly.models.roster.future.FutureDay
import com.crewly.models.roster.future.FutureDaysPattern
import com.crewly.models.account.CrewType
import com.crewly.models.duty.DutyType
import com.crewly.models.roster.future.EventTypesByDate
import io.reactivex.Single
import kotlin.math.max

internal class RosterFutureDaysCalculator(
  private val accountRepository: AccountRepository
) {

  private class DaysCounter(
    var firstDaysOnCount: Int = 0,
    var firstDaysOffCount: Int = 0,
    var secondDaysOnCount: Int = 0,
    var secondDaysOffCount: Int = 0
  )

  fun generateFutureRosterDays(
    crewType: CrewType,
    eventTypesByDate: List<EventTypesByDate>
  ): Single<List<FutureDay>> =
    accountRepository.getCurrentAccount()
      .map { account ->
        val pattern = getFutureDaysPattern(
          accountPattern = account.futureDaysPattern,
          crewType = crewType
        )

        val daysCounter = if (pattern.areFirstAndSecondPatternsDifferent()) {
          countDaysForMultiPattern(
            pattern = pattern,
            eventTypesByDate = eventTypesByDate
          )
        } else {
          countDaysForSinglePattern(
            pattern = pattern,
            eventTypesByDate = eventTypesByDate
          )
        }

        generateFutureDays(
          pattern = pattern,
          daysCounter = daysCounter,
          lastRosterDay = eventTypesByDate.last(),
          singlePattern = !pattern.areFirstAndSecondPatternsDifferent()
        )
      }

  private fun countDaysForSinglePattern(
    pattern: FutureDaysPattern,
    eventTypesByDate: List<EventTypesByDate>
  ): DaysCounter {
    val counter = DaysCounter()
    val numberOfRosterDays = eventTypesByDate.size
    val lastRosterDay = eventTypesByDate.last()
    val isLastDayOffDay = lastRosterDay.events.find { event -> event.isOff() } != null

    if (isLastDayOffDay) {
      val daysOffCount = countNumberOfConsecutiveDaysOff(
        eventTypesByDate = eventTypesByDate,
        startIndex = numberOfRosterDays - 1,
        daysOffThreshold = pattern.firstNumberOfDaysOff
      )

      counter.firstDaysOffCount = daysOffCount
    } else {
      val daysOnCount = countNumberOfConsecutiveDaysOn(
        eventTypesByDate = eventTypesByDate,
        startIndex = numberOfRosterDays - 1,
        daysOnThreshold = pattern.firstNumberOfDaysOn
      )

      counter.firstDaysOnCount = daysOnCount
    }

    return counter
  }

  private fun countDaysForMultiPattern(
    pattern: FutureDaysPattern,
    eventTypesByDate: List<EventTypesByDate>
  ): DaysCounter {
    val counter = DaysCounter()
    val numberOfRosterDays = eventTypesByDate.size
    val lastRosterDay = eventTypesByDate.last()
    val isLastDayOffDay = lastRosterDay.events.find { event -> event.isOff() } != null

    if (isLastDayOffDay) {
      val daysOffCount = countNumberOfConsecutiveDaysOff(
        eventTypesByDate = eventTypesByDate,
        startIndex = numberOfRosterDays - 1,
        daysOffThreshold = max(pattern.firstNumberOfDaysOff, pattern.secondNumberOfDaysOff)
      )

      if (daysOffCount <= pattern.firstNumberOfDaysOff && daysOffCount <= pattern.secondNumberOfDaysOff) {
        val daysOnCount = countNumberOfConsecutiveDaysOn(
          eventTypesByDate = eventTypesByDate,
          startIndex = numberOfRosterDays - daysOffCount - 1,
          daysOnThreshold = max(pattern.firstNumberOfDaysOn, pattern.secondNumberOfDaysOn)
        )

        when {
          daysOnCount > pattern.firstNumberOfDaysOn && daysOnCount <= pattern.secondNumberOfDaysOn -> {
            counter.secondDaysOffCount = daysOffCount
          }

          daysOnCount > pattern.secondNumberOfDaysOn && daysOnCount <= pattern.firstNumberOfDaysOn -> {
            counter.firstDaysOffCount = daysOffCount
          }

          else -> {
            counter.firstDaysOffCount = daysOffCount
          }
        }
      }
    } else {
      val daysOnCount = countNumberOfConsecutiveDaysOn(
        eventTypesByDate = eventTypesByDate,
        startIndex = numberOfRosterDays - 1,
        daysOnThreshold = max(pattern.firstNumberOfDaysOn, pattern.secondNumberOfDaysOn)
      )

      if (daysOnCount <= pattern.firstNumberOfDaysOn && daysOnCount <= pattern.secondNumberOfDaysOn) {
        val daysOffCount = countNumberOfConsecutiveDaysOff(
          eventTypesByDate = eventTypesByDate,
          startIndex = numberOfRosterDays - daysOnCount - 1,
          daysOffThreshold = max(pattern.firstNumberOfDaysOff, pattern.secondNumberOfDaysOff)
        )

        when {
          daysOffCount > pattern.firstNumberOfDaysOff && daysOffCount <= pattern.secondNumberOfDaysOff -> {
            counter.secondDaysOnCount = daysOnCount
          }

          daysOffCount > pattern.secondNumberOfDaysOff && daysOffCount <= pattern.firstNumberOfDaysOff -> {
            counter.firstDaysOnCount = daysOnCount
          }

          else -> {
            counter.firstDaysOnCount = daysOnCount
          }
        }
      }
    }

    return counter
  }

  private fun countNumberOfConsecutiveDaysOff(
    eventTypesByDate: List<EventTypesByDate>,
    startIndex: Int,
    daysOffThreshold: Int
  ): Int {
    var result = 0
    loop@ for (i in 0 until daysOffThreshold) {
      val rosterIndex = startIndex - i

      // No more days to loop through
      if (rosterIndex < 0) break@loop

      val rosterDay = eventTypesByDate[rosterIndex]
      val isDayOff = rosterDay.events.find { event -> event.isOff() } != null
      if (!isDayOff) {
        result = i
        break@loop
      }
    }

    return result + 1
  }

  private fun countNumberOfConsecutiveDaysOn(
    eventTypesByDate: List<EventTypesByDate>,
    startIndex: Int,
    daysOnThreshold: Int
  ): Int {
    var result = 0
    loop@ for (i in 1 until daysOnThreshold) {
      val rosterIndex = startIndex - i

      // No more days to loop through
      if (rosterIndex < 0) break@loop

      val rosterDay = eventTypesByDate[rosterIndex]
      val isDayOff = rosterDay.events.find { event -> event.isOff() } != null
      if (isDayOff) {
        result = i
        break@loop
      }
    }

    return result + 1
  }

  private fun generateFutureDays(
    pattern: FutureDaysPattern,
    daysCounter: DaysCounter,
    lastRosterDay: EventTypesByDate,
    singlePattern: Boolean
  ): List<FutureDay> {
    val futureRosterDays = mutableListOf<FutureDay>()
    val lastRosterDate = lastRosterDay.date
    val monthEndDate = lastRosterDate.dayOfMonth().withMaximumValue()
    val lastDate = 365 + (monthEndDate.dayOfMonth - lastRosterDate.dayOfMonth) + 1

    for (i in 1 until lastDate) {

      val eventType = daysCounter.run {
        when {
          firstDaysOnCount > 0 -> {
            if (firstDaysOnCount++ >= pattern.firstNumberOfDaysOn) {
              firstDaysOnCount = 0
              firstDaysOffCount = 1
            }

            DutyType.UNKNOWN
          }

          firstDaysOffCount > 0 -> {
            if (firstDaysOffCount++ >= pattern.firstNumberOfDaysOff) {
              firstDaysOffCount = 0
              if (singlePattern) firstDaysOnCount = 1 else secondDaysOnCount = 1
            }

            DutyType.TYPE_OFF
          }

          secondDaysOnCount > 0 -> {
            if (secondDaysOnCount++ >= pattern.secondNumberOfDaysOn) {
              secondDaysOnCount = 0
              secondDaysOffCount = 1
            }

            DutyType.TYPE_OFF
          }

          secondDaysOffCount > 0 -> {
            if (secondDaysOffCount++ >= pattern.secondNumberOfDaysOff) {
              secondDaysOffCount = 0
              firstDaysOnCount = 1
            }

            DutyType.TYPE_OFF
          }

          else -> DutyType.UNKNOWN
        }
      }

      val futureDay = FutureDay(
        date = lastRosterDate.plusDays(i),
        type = DutyType(
          name = eventType,
          code = "OFF"
        )
      )

      futureRosterDays.add(futureDay)
    }

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
}