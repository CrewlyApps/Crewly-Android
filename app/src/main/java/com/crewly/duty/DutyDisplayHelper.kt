package com.crewly.duty

import com.crewly.R
import com.crewly.account.AccountManager
import com.crewly.models.Company
import com.crewly.models.duty.DutyType
import com.crewly.models.flight.Flight
import com.crewly.models.roster.RosterPeriod
import com.crewly.utils.TimeDisplay
import org.joda.time.Period
import java.text.NumberFormat
import javax.inject.Inject

/**
 * Created by Derek on 21/04/2019
 */
class DutyDisplayHelper @Inject constructor(
  private val accountManager: AccountManager,
  private val timeDisplay: TimeDisplay
) {

  data class DutySummaryInfoData(
    val showCurrentTimezone: Boolean,
    val showReportLocalTime: Boolean,
    val showLandingLocalTime: Boolean,
    val showFlightDutyPeriod: Boolean
  )

  data class DutyDisplayInfo(
    val totalNumberOfFlights: Int = 0,
    val totalFlightDuration: String = "",
    val totalDutyTime: String = "",
    val totalFlightDutyPeriod: String = "",
    val totalSalary: String = ""
  )

  private data class DateData(
    val rosterDate: RosterPeriod.RosterDate,
    val totalFlights: Int,
    val flightsDuration: Period
  )

  companion object {
    private const val REPORT_TIME_EXTRA_DURATION_MINS = 45
    private const val DUTY_TIME_EXTRA_DURATION_MINS = 30
  }

  private val numberFormatter = NumberFormat.getNumberInstance().apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 2
  }

  fun getSummaryInfoDataForCompany(
    company: Company,
    flights: List<Flight>
  ) =
    DutySummaryInfoData(
      showCurrentTimezone = true,
      showReportLocalTime = company != Company.Norwegian && flights.isNotEmpty(),
      showLandingLocalTime = flights.isNotEmpty(),
      showFlightDutyPeriod = company != Company.Norwegian && flights.isNotEmpty()
    )

  fun getDutyDisplayInfo(
    rosterDates: List<RosterPeriod.RosterDate>
  ): DutyDisplayInfo =
    rosterDates
      .fold(mutableListOf<DateData>()) { dateData, rosterDate ->
        dateData.add(DateData(
          rosterDate = rosterDate,
          totalFlights = rosterDate.flights.size,
          flightsDuration = rosterDate.flights.fold(Period()) { totalPeriod, flight ->
            totalPeriod.plus(flight.getFlightDuration())
          }
        ))
        dateData
      }
      .run {
        DutyDisplayInfo(
          totalNumberOfFlights = this.sumBy { dateData -> dateData.totalFlights },
          totalFlightDuration = getFlightDuration(this),
          totalDutyTime = getTotalDutyTime(this),
          totalFlightDutyPeriod = getTotalFlightDutyPeriod(this),
          totalSalary = getSalary(this)
        )
      }

  fun getDutyIcon(
    dutyType: DutyType
  ): Int =
    when {
      dutyType.isAirportStandby() -> R.drawable.icon_asby
      dutyType.isHomeStandby() -> R.drawable.icon_home
      dutyType.isOff() -> R.drawable.icon_off
      dutyType.isAnnualLeave() -> R.drawable.icon_annual_leave
      dutyType.isSick() -> R.drawable.icon_sick
      dutyType.isParentalLeave() -> R.drawable.icon_parental_leave
      else -> -1
    }

  private fun getFlightDuration(
    dateData: List<DateData>
  ): String =
    timeDisplay.buildDisplayTimePeriod(
      period = dateData.fold(Period()) { totalFlightDuration, data ->
        totalFlightDuration.plus(data.flightsDuration)
      }
    )

  private fun getTotalDutyTime(
    dateData: List<DateData>
  ): String =
    timeDisplay.buildDisplayTimePeriod(
      period = dateData.fold(Period()) { totalDutyTime, data ->
        totalDutyTime.plus(calculateDutyTimeForDay(
          dateData = data
        ))
      }
    )

  private fun calculateDutyTimeForDay(
    dateData: DateData
  ): Period {
    val firstFlightInDay = dateData.rosterDate.flights.firstOrNull()
    val lastFlightInDay = dateData.rosterDate.flights.lastOrNull()
    if (firstFlightInDay == null || lastFlightInDay == null) return Period(0)
    return Period(
      firstFlightInDay.departureTime,
      lastFlightInDay.arrivalTime.plusMinutes(DUTY_TIME_EXTRA_DURATION_MINS)
    ).plusMinutes(REPORT_TIME_EXTRA_DURATION_MINS)
  }

  private fun getTotalFlightDutyPeriod(
    dateData: List<DateData>
  ): String =
    timeDisplay.buildDisplayTimePeriod(
      period = dateData.fold(Period()) { totalDutyTime, data ->
        totalDutyTime.plus(calculateFlightDutyPeriod(
          dateData = data
        ))
      }
    )

  private fun calculateFlightDutyPeriod(
    dateData: DateData
  ): Period {
    val firstFlightInDay = dateData.rosterDate.flights.firstOrNull()
    val lastFlightInDay = dateData.rosterDate.flights.lastOrNull()
    if (firstFlightInDay == null || lastFlightInDay == null) return Period(0)
    return Period(firstFlightInDay.departureTime, lastFlightInDay.arrivalTime)
      .plusMinutes(REPORT_TIME_EXTRA_DURATION_MINS)
  }

  private fun getSalary(
    dateData: List<DateData>
  ): String {
    val account = accountManager.getCurrentAccount()
    if (!account.salary.hasSalaryInfo()) return ""

    val salary = account.salary
    val baseSalaryPerDay = (salary.perMonthBase * 12) / 365f

    val totalSalary = dateData.fold(0f) { totalSalary, data ->
      val extraSalary = when {
        account.base.isNotBlank() && data.rosterDate.flights.firstOrNull()?.departureAirport?.city != account.base -> {
          salary.perFlightHourOob
        }

        data.rosterDate.duties.find { duty -> duty.type.isAirportStandby() } != null -> {
          salary.perAsbyHour
        }

        data.rosterDate.duties.find { duty -> duty.type.isHomeStandby() } != null -> {
          salary.perHsbyHour
        }

        else -> salary.perFlightHour
      }

      val extraSalaryPerMinute = extraSalary / 60f
      val totalDutyTime = calculateDutyTimeForDay(
        dateData = data
      )

      totalSalary + baseSalaryPerDay + (extraSalaryPerMinute * totalDutyTime.toStandardMinutes().minutes)
    }

    return numberFormatter.format(totalSalary)
  }
}