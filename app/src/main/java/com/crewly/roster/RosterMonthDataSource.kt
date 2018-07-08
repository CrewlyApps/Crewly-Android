package com.crewly.roster

import android.arch.paging.ItemKeyedDataSource
import com.crewly.app.CrewlyDatabase
import com.crewly.duty.DutyType
import com.crewly.duty.Sector
import com.crewly.utils.plus
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import org.joda.time.DateTime

/**
 * Created by Derek on 01/07/2018
 * Data source for loading [RosterPeriod.RosterMonth] in to a paged list.
 */
class RosterMonthDataSource(private val crewlyDatabase: CrewlyDatabase,
                            private val disposables: CompositeDisposable):
        ItemKeyedDataSource<DateTime, RosterPeriod.RosterMonth>() {

    override fun loadInitial(params: LoadInitialParams<DateTime>,
                             callback: LoadInitialCallback<RosterPeriod.RosterMonth>) {
        val month = params.requestedInitialKey
        if (month != null) {
            loadRosterMonth(month, { rosterMonth ->
                if (rosterMonth != null) {
                    callback.onResult(mutableListOf(rosterMonth))
                } else {
                    callback.onResult(mutableListOf<RosterPeriod.RosterMonth>())
                }
            })
        }
    }

    override fun loadAfter(params: LoadParams<DateTime>,
                           callback: LoadCallback<RosterPeriod.RosterMonth>) {
        val lastMonth = params.key
        val month = lastMonth.dayOfMonth().withMinimumValue().withTimeAtStartOfDay()
        loadRosterMonth(month.plusMonths(1), { rosterMonth ->
            if (rosterMonth != null) {
                callback.onResult(mutableListOf(rosterMonth))
            } else {
                callback.onResult(mutableListOf<RosterPeriod.RosterMonth>())
            }
        })
    }

    override fun loadBefore(params: LoadParams<DateTime>,
                            callback: LoadCallback<RosterPeriod.RosterMonth>) {}

    override fun getKey(item: RosterPeriod.RosterMonth): DateTime = item.rosterDates[0].date

    /**
     * Loads a particular [RosterPeriod.RosterMonth].
     *
     * @param month The month to load. Will use the current time set on the month and fetch one
     * month's worth of data from that time.
     *
     * @param loadAction Action to perform after a successful load. Action returns an empty list if
     * there is no more data.
     */
    private fun loadRosterMonth(month: DateTime,
                                loadAction: (rosterMonth: RosterPeriod.RosterMonth?) -> Unit) {
        val nextMonth = month.plusMonths(1).minusDays(1)

        disposables + crewlyDatabase.dutyDao()
                .fetchDutiesBetween(month.millis, nextMonth.millis)
                .zipWith(crewlyDatabase.sectorDao().fetchSectorsBetween(month.millis, nextMonth.millis),
                        object: BiFunction<List<DutyType>, List<Sector>, RosterPeriod.RosterMonth> {
                    override fun apply(duties: List<DutyType>, sectors: List<Sector>): RosterPeriod.RosterMonth {
                        val rosterMonth = RosterPeriod.RosterMonth()
                        var sectorsAdded = 0

                        duties.forEach {
                            val dutyDate = it.date
                            val rosterDate = RosterPeriod.RosterDate(dutyDate, it)

                            run loop@ {
                                sectors.drop(sectorsAdded).forEach {
                                    if (dutyDate.dayOfMonth == it.departureTime.dayOfMonth) {
                                        rosterDate.sectors.add(it)
                                        sectorsAdded++
                                    } else {
                                        return@loop
                                    }
                                }
                            }

                            rosterMonth.rosterDates.add(rosterDate)
                        }

                        return rosterMonth
                    }
                })
                .take(1)
                .subscribe { rosterMonth ->
                    if (rosterMonth.rosterDates.isEmpty()) {
                        loadAction.invoke(null)
                    } else {
                        loadAction.invoke(rosterMonth)
                    }
                }
    }
}