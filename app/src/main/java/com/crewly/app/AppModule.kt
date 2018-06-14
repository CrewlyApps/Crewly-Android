package com.crewly.app

import android.app.Application
import android.arch.persistence.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Derek on 27/05/2018
 */
@Module
class AppModule {

    @Singleton
    @Provides
    fun provideCrewlyDatabase(app: Application): CrewlyDatabase = Room.databaseBuilder(app,
            CrewlyDatabase::class.java, "Crewly Database").build()
}