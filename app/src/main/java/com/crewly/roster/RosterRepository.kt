package com.crewly.roster

import com.crewly.account.AccountManager
import com.crewly.app.CrewlyDatabase
import com.crewly.duty.Airport
import com.crewly.duty.Duty
import com.crewly.duty.Sector
import com.crewly.models.DateTimePeriod
import com.crewly.utils.createTestRosterMonth
import com.crewly.utils.withTimeAtEndOfDay
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * Created by Derek on 02/06/2018
 */
class RosterRepository @Inject constructor(
  private val accountManager: AccountManager,
  private val crewlyDatabase: CrewlyDatabase
) {

  fun fetchRoster(): Single<List<RosterPeriod.RosterMonth>> {
    val rosterList = listOf(createTestRosterMonth(), createTestRosterMonth(),
      createTestRosterMonth(), createTestRosterMonth(), createTestRosterMonth())
    return Single.just(rosterList.toList())
  }

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

    return crewlyDatabase.dutyDao()
      .fetchDutiesBetween(crewCode, month.millis, nextMonth.millis)
      .zipWith(crewlyDatabase.sectorDao().fetchSectorsBetween(crewCode, month.millis, nextMonth.millis),
        BiFunction<List<Duty>, List<Sector>, RosterPeriod.RosterMonth> { duties, sectors ->
          val rosterMonth = RosterPeriod.RosterMonth()
          rosterMonth.rosterDates = combineDutiesAndSectorsToRosterDates(duties, sectors)
          rosterMonth
        })
  }

  /**
   * Loads all [RosterPeriod.RosterDate] for the given [dateTimePeriod]
   */
  fun fetchRosterDays(dateTimePeriod: DateTimePeriod): Single<List<RosterPeriod.RosterDate>> {
    val account = accountManager.getCurrentAccount()
    val firstDay = dateTimePeriod.startDateTime.withTimeAtStartOfDay()
    val lastDay = dateTimePeriod.endDateTime.withTimeAtEndOfDay()

    return crewlyDatabase.dutyDao()
      .fetchDutiesBetween(
        crewCode = account.crewCode,
        startTime = firstDay.millis,
        endTime = lastDay.millis
      )
      .zipWith(crewlyDatabase.sectorDao()
        .fetchSectorsBetween(
          crewCode = account.crewCode,
          startTime = firstDay.millis,
          endTime = lastDay.millis
        ),
        BiFunction<List<Duty>, List<Sector>, List<RosterPeriod.RosterDate>> { duties, sectors ->
          combineDutiesAndSectorsToRosterDates(duties, sectors)
        })
  }

  fun fetchDutiesForDay(date: DateTime): Flowable<List<Duty>> {
    val startTime = date.withTimeAtStartOfDay().millis
    val endTime = date.plusDays(1).withTimeAtStartOfDay().minusMillis(1).millis
    return crewlyDatabase.dutyDao().observeDutiesBetween(startTime, endTime)
  }

  fun fetchSectorsFromDayOnwards(
    crewCode: String,
    date: DateTime
  ): Single<List<Sector>> =
    crewlyDatabase
      .sectorDao()
      .fetchSectorsBetween(
        crewCode = crewCode,
        startTime = date.millis,
        endTime = date.plusYears(1).millis
      )

  fun fetchSectorsForDay(date: DateTime): Flowable<List<Sector>> {
    val startTime = date.withTimeAtStartOfDay().millis
    val endTime = date.plusDays(1).withTimeAtStartOfDay().minusMillis(1).millis
    return crewlyDatabase.sectorDao().observeSectorsBetween(startTime, endTime)
  }

  fun fetchAirportsForSectors(sectors: List<Sector>): Single<List<Airport>> =
    crewlyDatabase.airportDao().fetchAirports(
      codes = sectors.fold(mutableSetOf<String>()) { airportCodes, sector ->
        airportCodes.add(sector.departureAirport)
        airportCodes.add(sector.arrivalAirport)
        airportCodes
      }.toList()
    )

  fun fetchDepartureAirportForSector(sector: Sector): Single<Airport> =
    crewlyDatabase.airportDao().fetchAirport(sector.departureAirport)

  fun fetchArrivalAirportForSector(sector: Sector): Single<Airport> =
    crewlyDatabase.airportDao().fetchAirport(sector.arrivalAirport)

  /**
   * Combines a list of [duties] and [sectors] to [RosterPeriod.RosterDate]. All [duties]
   * and [sectors] will be added to the corresponding [RosterPeriod.RosterDate].
   */
  private fun combineDutiesAndSectorsToRosterDates(
    duties: List<Duty>,
    sectors: List<Sector>
  ):
    MutableList<RosterPeriod.RosterDate> {
    val rosterDates = mutableListOf<RosterPeriod.RosterDate>()

    if (duties.isEmpty()) {
      return rosterDates
    }

    var currentDutyDate = duties.first().date
    var dutiesPerDay = mutableListOf<Duty>()
    var sectorsAdded = 0

    duties.forEach {
      val dutyDate = it.date
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

  private fun createNewRosterDate(duties: MutableList<Duty>): RosterPeriod.RosterDate {
    val firstDuty = duties[0]
    return RosterPeriod.RosterDate(firstDuty.date, duties)
  }

  private fun addSectorsToRosterDate(rosterDate: RosterPeriod.RosterDate, remainingSectors: List<Sector>) {
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
}