package com.crewly.app

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.crewly.account.Account
import com.crewly.account.AccountDao
import com.crewly.duty.*

/**
 * Created by Derek on 13/06/2018
 */
@Database(entities = [Account::class, Airport::class, Duty::class, Sector::class], version = 1)
@TypeConverters(DatabaseConverters::class)
abstract class CrewlyDatabase: RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun airportDao(): AirportDao
    abstract fun dutyDao(): DutyDao
    abstract fun sectorDao(): SectorDao
}