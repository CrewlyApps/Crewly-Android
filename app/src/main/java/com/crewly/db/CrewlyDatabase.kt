package com.crewly.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crewly.db.account.Account
import com.crewly.db.account.AccountDao
import com.crewly.db.airport.Airport
import com.crewly.db.airport.AirportDao
import com.crewly.db.crew.Crew
import com.crewly.db.crew.CrewDao
import com.crewly.db.duty.Duty
import com.crewly.db.duty.DutyDao
import com.crewly.db.sector.Sector
import com.crewly.db.sector.SectorDao

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