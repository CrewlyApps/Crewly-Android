package com.crewly.app

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crewly.account.Account
import com.crewly.account.AccountDao
import com.crewly.db.crew.Crew
import com.crewly.db.crew.CrewDao
import com.crewly.db.airport.Airport
import com.crewly.db.airport.AirportDao
import com.crewly.duty.*

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