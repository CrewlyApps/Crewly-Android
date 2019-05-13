package com.crewly.utils

import com.crewly.duty.Duty
import com.crewly.duty.ryanair.RyanairDutyType
import com.crewly.duty.Sector
import com.crewly.roster.RosterPeriod
import com.crewly.roster.RosterPeriod.RosterDate
import org.joda.time.DateTime

/**
 * Created by Derek on 02/06/2018
 */

fun createTestRosterMonth(): RosterPeriod.RosterMonth {
  val rosterDates = mutableListOf<RosterDate>().apply {
    add(RosterDate(
      date = DateTime(1),
      duties = mutableListOf(Duty(type = RyanairDutyType.AIRPORT_STANDBY.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(10),
      duties = mutableListOf(Duty(type = RyanairDutyType.HOME_STANDBY.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(100),
      duties = mutableListOf(Duty(type = RyanairDutyType.SICK.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(1000),
      duties = mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(10000),
      duties = mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(100000),
      duties = mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(1000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(10000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(100000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(1000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(10000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(100000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(1000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(2000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(3000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(4000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(5000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(6000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(7000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(8000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.AIRPORT_STANDBY.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(9000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.AIRPORT_STANDBY.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(10000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(20000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(30000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.FLIGHT.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(40000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.HOME_STANDBY.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(50000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.HOME_STANDBY.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(60000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.HOME_STANDBY.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(70000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.SICK.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(80000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.SICK.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(90000000000000),
      duties = mutableListOf(Duty(type = RyanairDutyType.SICK.dutyName)),
      sectors = createTestSectors()
    ))

    add(RosterDate(
      date = DateTime(99999999999999),
      duties = mutableListOf(Duty(type = RyanairDutyType.OFF.dutyName)),
      sectors = createTestSectors()
    ))
  }


  return RosterPeriod.RosterMonth(rosterDates)
}

fun createTestSectors(): MutableList<Sector> = mutableListOf(
  Sector(
    flightId = "2"
  )
)