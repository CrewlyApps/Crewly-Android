package com.crewly.roster

import android.arch.paging.DataSource
import com.crewly.app.CrewlyDatabase
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime

/**
 * Created by Derek on 07/07/2018
 * Factory for creating [RosterMonthDataSource] objects.
 */
class RosterMonthDataSourceFactory(private val crewlyDatabase: CrewlyDatabase,
                                   private val disposables: CompositeDisposable):
        DataSource.Factory<DateTime, RosterPeriod.RosterMonth>() {

    override fun create(): DataSource<DateTime, RosterPeriod.RosterMonth> =
            RosterMonthDataSource(crewlyDatabase, disposables)
}