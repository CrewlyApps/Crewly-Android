package com.crewly.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crewly.persistence.DatabaseConverters
import com.crewly.persistence.account.Account
import com.crewly.persistence.account.AccountDao
import com.crewly.persistence.airport.Airport
import com.crewly.persistence.airport.AirportDao
import com.crewly.persistence.crew.Crew
import com.crewly.persistence.crew.CrewDao
import com.crewly.persistence.duty.Duty
import com.crewly.persistence.duty.DutyDao
import com.crewly.persistence.sector.Sector
import com.crewly.persistence.sector.SectorDao

/**
 * Created by Derek on 13/06/2018
 */
@Database(entities = [
  Account::class,
  Airport::class,
  Crew::class,
  Duty::class,
  Sector::class
], version = 1)
@TypeConverters(DatabaseConverters::class)
abstract class CrewlyDatabase: RoomDatabase() {

  abstract fun accountDao(): AccountDao
  abstract fun airportDao(): AirportDao
  abstract fun crewDao(): CrewDao
  abstract fun dutyDao(): DutyDao
  abstract fun sectorDao(): SectorDao
}