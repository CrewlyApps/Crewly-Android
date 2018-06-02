package com.crewly.app

import com.crewly.roster.RosterRepository
import dagger.Module
import dagger.Provides

/**
 * Created by Derek on 02/06/2018
 */
@Module
class RepositoryModule {

    @Provides
    fun provideRosterRepository(): RosterRepository = RosterRepository()
}