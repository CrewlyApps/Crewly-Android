package com.crewly.app

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.crewly.auth.Account
import com.crewly.auth.AccountDao
import com.crewly.duty.DutyDao
import com.crewly.duty.DutyType
import com.crewly.duty.Sector
import com.crewly.duty.SectorDao

/**
 * Created by Derek on 13/06/2018
 */
@Database(entities = [Account::class, DutyType::class, Sector::class], version = 1)
@TypeConverters(DatabaseConverters::class)
abstract class CrewlyDatabase: RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun dutyDao(): DutyDao
    abstract fun sectorDao(): SectorDao
}