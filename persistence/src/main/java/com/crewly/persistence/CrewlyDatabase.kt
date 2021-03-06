package com.crewly.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crewly.persistence.account.DbAccount
import com.crewly.persistence.account.AccountDao
import com.crewly.persistence.airport.DbAirport
import com.crewly.persistence.airport.AirportDao
import com.crewly.persistence.crew.DbCrew
import com.crewly.persistence.crew.CrewDao
import com.crewly.persistence.duty.DbDuty
import com.crewly.persistence.duty.DutyDao
import com.crewly.persistence.roster.DbRawRoster
import com.crewly.persistence.roster.RawRosterDao
import com.crewly.persistence.flight.DbFlight
import com.crewly.persistence.flight.FlightDao

/**
 * Created by Derek on 13/06/2018
 */
@Database(entities = [
  DbAccount::class,
  DbAirport::class,
  DbCrew::class,
  DbDuty::class,
  DbFlight::class,
  DbRawRoster::class
], version = 1)
@TypeConverters(DatabaseConverters::class)
abstract class CrewlyDatabase: RoomDatabase() {

  abstract fun accountDao(): AccountDao
  abstract fun airportDao(): AirportDao
  abstract fun crewDao(): CrewDao
  abstract fun dutyDao(): DutyDao
  abstract fun flightDao(): FlightDao
  abstract fun rawRosterDao(): RawRosterDao
}