package com.crewly.roster

import com.crewly.app.CrewlyDatabase
import com.crewly.duty.Airport
import com.crewly.duty.DutyType
import com.crewly.duty.Sector
import com.crewly.utils.createTestRosterMonth
import io.reactivex.Flowable
import io.reactivex.Single
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Derek on 02/06/2018
 */
@Singleton
class RosterRepository @Inject constructor(private val crewlyDatabase: CrewlyDatabase) {

    fun fetchRoster(): Single<List<RosterPeriod.RosterMonth>> {
        val rosterList = listOf(createTestRosterMonth(), createTestRosterMonth(),
                createTestRosterMonth(), createTestRosterMonth(), createTestRosterMonth())
        return Single.just(rosterList.toList())
    }

    fun fetchDutiesForDay(date: DateTime): Flowable<List<DutyType>> {
        val startTime = date.withTimeAtStartOfDay().millis
        val endTime = date.plusDays(1).withTimeAtStartOfDay().minusMillis(1).millis
        return crewlyDatabase.dutyDao().fetchDutiesBetween(startTime, endTime)
    }

    fun fetchSectorsForDay(date: DateTime): Flowable<List<Sector>> {
        val startTime = date.withTimeAtStartOfDay().millis
        val endTime = date.plusDays(1).withTimeAtStartOfDay().minusMillis(1).millis
        return crewlyDatabase.sectorDao().fetchSectorsBetween(startTime, endTime)
    }

    fun fetchDepartureAirportForSector(sector: Sector): Single<Airport> =
            crewlyDatabase.airportDao().fetchAirport(sector.departureAirport)

    fun fetchArrivalAirportForSector(sector: Sector): Single<Airport> =
            crewlyDatabase.airportDao().fetchAirport(sector.arrivalAirport)
}