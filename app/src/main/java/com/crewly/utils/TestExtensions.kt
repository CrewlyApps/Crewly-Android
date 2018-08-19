package com.crewly.utils

import com.crewly.duty.Duty
import com.crewly.duty.RyanairDutyType
import com.crewly.duty.Sector
import com.crewly.roster.RosterPeriod
import com.crewly.roster.RosterPeriod.RosterDate
import org.joda.time.DateTime

/**
 * Created by Derek on 02/06/2018
 */

fun createTestRosterMonth(): RosterPeriod.RosterMonth {
    val rosterDates: MutableList<RosterDate> = mutableListOf()
    rosterDates.add(RosterDate(DateTime(1), mutableListOf(Duty(type = RyanairDutyType.AIRPORT_STANDBY.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(10), mutableListOf(Duty(type = RyanairDutyType.HOME_STANDBY.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(100), mutableListOf(Duty(type = RyanairDutyType.SICK.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(1000), mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(10000), mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(100000), mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(1000000), mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(10000000), mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(100000000), mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(1000000000), mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(10000000000), mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(100000000000), mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(1000000000000), mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(2000000000000), mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(3000000000000), mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(4000000000000), mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(5000000000000), mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(6000000000000), mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(7000000000000), mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(8000000000000), mutableListOf(Duty(type = RyanairDutyType.AIRPORT_STANDBY.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(9000000000000), mutableListOf(Duty(type = RyanairDutyType.AIRPORT_STANDBY.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(10000000000000), mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(20000000000000), mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(30000000000000), mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(40000000000000), mutableListOf(Duty(type = RyanairDutyType.HOME_STANDBY.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(50000000000000), mutableListOf(Duty(type = RyanairDutyType.HOME_STANDBY.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(60000000000000), mutableListOf(Duty(type = RyanairDutyType.HOME_STANDBY.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(70000000000000), mutableListOf(Duty(type = RyanairDutyType.SICK.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(80000000000000), mutableListOf(Duty(type = RyanairDutyType.SICK.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(90000000000000), mutableListOf(Duty(type = RyanairDutyType.SICK.dutyName)), createTestSectors()))
    rosterDates.add(RosterDate(DateTime(99999999999999), mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)), createTestSectors()))
    return RosterPeriod.RosterMonth(rosterDates)
}

fun createTestSectors(): MutableList<Sector> = mutableListOf(Sector("2"))