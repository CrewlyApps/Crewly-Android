package com.crewly.roster

import com.crewly.account.AccountManager
import com.crewly.app.CrewlyDatabase
import com.crewly.duty.Airport
import com.crewly.duty.Duty
import com.crewly.duty.Sector
import com.crewly.utils.createTestRosterMonth
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Derek on 02/06/2018
 */
@Singleton
class RosterRepository @Inject constructor(private val accountManager: AccountManager,
                                           private val crewlyDatabase: CrewlyDatabase) {

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
    fun fetchRosterMonth(month: DateTime): Single<RosterPeriod.RosterMonth> {
        val account = accountManager.getCurrentAccount()
        val nextMonth = month.plusMonths(1).minusHours(1)

        return crewlyDatabase.dutyDao()
                .fetchDutiesBetween(account.crewCode, month.millis, nextMonth.millis)
                .zipWith(crewlyDatabase.sectorDao().fetchSectorsBetween(account.crewCode, month.millis, nextMonth.millis),
                        object: BiFunction<List<Duty>, List<Sector>, RosterPeriod.RosterMonth> {
                            override fun apply(duties: List<Duty>, sectors: List<Sector>): RosterPeriod.RosterMonth {
                                val rosterMonth = RosterPeriod.RosterMonth()
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

                                        rosterMonth.rosterDates.add(rosterDate)
                                        dutiesPerDay = mutableListOf()
                                        currentDutyDate = dutyDate
                                    }

                                    dutiesPerDay.add(it)

                                    // Add the last roster date if it's the last day
                                    if (lastDuty) {
                                        val rosterDate = createNewRosterDate(dutiesPerDay)
                                        addSectorsToRosterDate(rosterDate, sectors.drop(sectorsAdded))
                                        sectorsAdded += rosterDate.sectors.size
                                        rosterMonth.rosterDates.add(rosterDate)
                                    }
                                }

                                return rosterMonth
                            }
                        })
    }

    fun fetchDutiesForDay(date: DateTime): Flowable<List<Duty>> {
        val startTime = date.withTimeAtStartOfDay().millis
        val endTime = date.plusDays(1).withTimeAtStartOfDay().minusMillis(1).millis
        return crewlyDatabase.dutyDao().observeDutiesBetween(startTime, endTime)
    }

    fun fetchSectorsForDay(date: DateTime): Flowable<List<Sector>> {
        val startTime = date.withTimeAtStartOfDay().millis
        val endTime = date.plusDays(1).withTimeAtStartOfDay().minusMillis(1).millis
        return crewlyDatabase.sectorDao().observeSectorsBetween(startTime, endTime)
    }

    fun fetchDepartureAirportForSector(sector: Sector): Single<Airport> =
            crewlyDatabase.airportDao().fetchAirport(sector.departureAirport)

    fun fetchArrivalAirportForSector(sector: Sector): Single<Airport> =
            crewlyDatabase.airportDao().fetchAirport(sector.arrivalAirport)

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