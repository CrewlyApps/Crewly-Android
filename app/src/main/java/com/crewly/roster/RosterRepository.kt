package com.crewly.roster

import android.app.Application
import com.crewly.utils.createTestRosterMonth
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Derek on 02/06/2018
 */
@Singleton
class RosterRepository @Inject constructor(private val application: Application) {

    fun fetchRoster(): Single<List<RosterPeriod.RosterMonth>> {
        val rosterList = listOf(createTestRosterMonth(), createTestRosterMonth(),
                createTestRosterMonth(), createTestRosterMonth(), createTestRosterMonth())
        return Single.just(rosterList.toList())
    }
}