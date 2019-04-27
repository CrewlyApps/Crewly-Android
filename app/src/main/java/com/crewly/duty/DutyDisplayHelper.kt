package com.crewly.duty

import com.crewly.roster.RosterPeriod
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import javax.inject.Inject

/**
 * Created by Derek on 21/04/2019
 */
class DutyDisplayHelper @Inject constructor() {

  data class DutyDisplayInfo(
    val totalNumberOfSectors: Int = 0,
    val totalFlightDuration: String = "",
    val totalDutyTime: String = "",
    val totalFlightDutyPeriod: String = ""
  )

  companion object {
    private const val DUTY_TIME_EXTRA_DURATION_MINS = 105
    private const val FLIGHT_DUTY_PERIOD_EXTRA_DURATION_MINS = 75
  }

  private val timeFormatter = PeriodFormatterBuilder()
    .appendHours()
    .appendSuffix("h ")
    .appendMinutes()
    .appendSuffix("m")
    .toFormatter()

  fun getDutyDisplayInfo(rosterDates: List<RosterPeriod.RosterDate>): DutyDisplayInfo =
    rosterDates
      .fold(mutableListOf<Sector>()) { sectors, rosterDate ->
        sectors.addAll(rosterDate.sectors)
        sectors
      }
      .fold(0 to Period()) { (totalSectors, totalPeriod), sector ->
        totalSectors + 1 to totalPeriod.plus(sector.getFlightDuration())
      }
      .run {
        DutyDisplayInfo(
          totalNumberOfSectors = this.first,
          totalFlightDuration = getFlightDuration(this.second),
          totalDutyTime = getDutyTime(this.second),
          totalFlightDutyPeriod = getFlightDutyPeriod(this.second)
        )
      }

  private fun getFlightDuration(flightsDuration: Period): String =
    timeFormatter.print(flightsDuration.normalizedStandard())

  private fun getDutyTime(flightsDuration: Period): String =
    timeFormatter.print(
      flightsDuration
      .plusMinutes(DUTY_TIME_EXTRA_DURATION_MINS)
      .normalizedStandard()
    )

  private fun getFlightDutyPeriod(flightsDuration: Period): String =
    timeFormatter.print(
      flightsDuration
        .plusMinutes(FLIGHT_DUTY_PERIOD_EXTRA_DURATION_MINS)
        .normalizedStandard()
    )
}