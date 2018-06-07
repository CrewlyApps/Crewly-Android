package com.crewly.utils

import com.crewly.roster.RosterPeriod
import com.crewly.roster.RosterPeriod.RosterDate
import com.crewly.roster.DutyType
import com.crewly.roster.DutyType.Sector
import org.joda.time.DateTime

/**
 * Created by Derek on 02/06/2018
 */

fun createTestRosterMonth(): RosterPeriod.RosterMonth {
    val rosterDates: MutableList<RosterDate> = mutableListOf()
    rosterDates.add(RosterDate(DateTime(1), DutyType.ASBY(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(10), DutyType.HSBY(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(100), DutyType.Sick(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(1000), DutyType.Duty(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(10000), DutyType.Duty(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(100000), DutyType.Duty(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(1000000), DutyType.Duty(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(10000000), DutyType.Duty(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(100000000), DutyType.Off(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(1000000000), DutyType.Off(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(10000000000), DutyType.Off(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(100000000000), DutyType.Off(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(1000000000000), DutyType.Off(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(2000000000000), DutyType.Off(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(3000000000000), DutyType.Off(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(4000000000000), DutyType.Off(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(5000000000000), DutyType.Duty(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(6000000000000), DutyType.Duty(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(7000000000000), DutyType.Duty(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(8000000000000), DutyType.ASBY(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(9000000000000), DutyType.ASBY(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(10000000000000), DutyType.Duty(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(20000000000000), DutyType.Duty(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(30000000000000), DutyType.Duty(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(40000000000000), DutyType.HSBY(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(50000000000000), DutyType.HSBY(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(60000000000000), DutyType.HSBY(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(70000000000000), DutyType.Sick(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(80000000000000), DutyType.Sick(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(90000000000000), DutyType.Sick(), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(99999999999999), DutyType.Off(), createTestSectors()))
    return RosterPeriod.RosterMonth(rosterDates)
}

fun createTestSectors(): List<Sector> = listOf(Sector("2"))