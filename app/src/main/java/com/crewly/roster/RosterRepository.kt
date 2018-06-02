package com.crewly.roster

import com.crewly.utils.createTestRosterMonth
import io.reactivex.Single

/**
 * Created by Derek on 02/06/2018
 */
class RosterRepository {

    fun fetchRoster(): Single<List<RosterPeriod.RosterMonth>> {
        val rosterList = listOf(createTestRosterMonth(), createTestRosterMonth(),
                createTestRosterMonth(), createTestRosterMonth(), createTestRosterMonth())
        return Single.just(rosterList.toList())
    }
}