package com.crewly.duty

import com.crewly.account.AccountManager
import com.crewly.models.roster.RosterPeriod
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import javax.inject.Inject

/**
 * Created by Derek on 21/04/2019
 */
class DutyDisplayHelper @Inject constructor(
  private val accountManager: AccountManager
) {

  data class DutyDisplayInfo(
    val totalNumberOfSectors: Int = 0,
    val totalFlightDuration: String = "",
    val totalDutyTime: String = "",
    val totalFlightDutyPeriod: String = "",
    val totalSalary: String = ""
  )

  private data class DateData(
    val rosterDate: RosterPeriod.RosterDate,
    val totalSectors: Int,
    val flightsDuration: Period
  )

  companion object {
    private const val DUTY_TIME_EXTRA_DURATION_MINS = 30
  }

  private val timeFormatter = PeriodFormatterBuilder()
    .appendHours()
    .appendSuffix("h ")
    .appendMinutes()
    .appendSuffix("m")
    .toFormatter()

  fun getDutyDisplayInfo(
    rosterDates: List<RosterPeriod.RosterDate>
  ): DutyDisplayInfo =
    rosterDates
      .fold(mutableListOf<DateData>()) { dateData, rosterDate ->
        dateData.add(DateData(
          rosterDate = rosterDate,
          totalSectors = rosterDate.sectors.size,
          flightsDuration = rosterDate.sectors.fold(Period()) { totalPeriod, sector ->
            totalPeriod.plus(sector.getFlightDuration())
          }
        ))
        dateData
      }
      .run {
        DutyDisplayInfo(
          totalNumberOfSectors = this.sumBy { dateData -> dateData.totalSectors },
          totalFlightDuration = getFlightDuration(this),
          totalDutyTime = getTotalDutyTime(this),
          totalFlightDutyPeriod = getTotalFlightDutyPeriod(this),
          totalSalary = getSalary(this)
        )
      }

  private fun getFlightDuration(
    dateData: List<DateData>
  ): String =
    timeFormatter.print(
      dateData.fold(Period()) { totalFlightDuration, data ->
        totalFlightDuration.plus(data.flightsDuration)
      }.normalizedStandard()
    )

  private fun getTotalDutyTime(
    dateData: List<DateData>
  ): String =
    timeFormatter.print(
      dateData.fold(Period()) { totalDutyTime, data ->
        totalDutyTime.plus(calculateDutyTimeForDay(
          dateData = data
        ))
      }.normalizedStandard()
    )

  private fun calculateDutyTimeForDay(
    dateData: DateData
  ): Period {
    val firstSectorInDay = dateData.rosterDate.sectors.firstOrNull()
    val lastSectorInDay = dateData.rosterDate.sectors.lastOrNull()
    if (firstSectorInDay == null || lastSectorInDay == null) return Period(0)
    return Period(firstSectorInDay.departureTime,
      lastSectorInDay.arrivalTime.plusMinutes(DUTY_TIME_EXTRA_DURATION_MINS))
  }

  private fun getTotalFlightDutyPeriod(
    dateData: List<DateData>
  ): String =
    timeFormatter.print(
      dateData.fold(Period()) { totalDutyTime, data ->
        totalDutyTime.plus(calculateFlightDutyPeriod(
          dateData = data
        ))
      }.normalizedStandard()
    )

  private fun calculateFlightDutyPeriod(
    dateData: DateData
  ): Period {
    val firstSectorInDay = dateData.rosterDate.sectors.firstOrNull()
    val lastSectorInDay = dateData.rosterDate.sectors.lastOrNull()
    if (firstSectorInDay == null || lastSectorInDay == null) return Period(0)
    return Period(firstSectorInDay.departureTime, lastSectorInDay.arrivalTime)
  }

  private fun getSalary(
    dateData: List<DateData>
  ): String {
    val account = accountManager.getCurrentAccount()
    if (!account.salary.hasSalaryInfo()) return "0"

    val salary = account.salary
    val baseSalaryPerMinute = salary.base / 60

    return dateData.fold(0f) { totalSalary, data ->
      val extraSalary = when {
        data.rosterDate.sectors.firstOrNull()?.departureAirport != account.base -> {
          salary.perFlightHourOob
        }

        data.rosterDate.fullDuties.find { duty -> duty.dutyType.isAirportStandby() } != null -> {
          salary.perAsbyHour
        }

        data.rosterDate.fullDuties.find { duty -> duty.dutyType.isHomeStandby() } != null -> {
          salary.perHsbyHour
        }

        data.rosterDate.fullDuties.find { duty -> duty.dutyType.isFlight() } != null -> {
          salary.perFlightHour
        }

        else -> 0f
      }

      val extraSalaryPerMinute = extraSalary / 60
      val totalDutyTime = calculateDutyTimeForDay(
        dateData = data
      )
      val totalBaseSalaryMinutes = totalDutyTime.hours * 60f
      val totalExtraSalaryMinutes = totalDutyTime.minutes % 60

      totalSalary +
        (totalBaseSalaryMinutes * baseSalaryPerMinute) +
        (totalExtraSalaryMinutes * extraSalaryPerMinute)
    }.toString()
  }
}