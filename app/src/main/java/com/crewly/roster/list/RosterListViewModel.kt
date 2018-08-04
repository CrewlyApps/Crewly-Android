package com.crewly.roster.list

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.crewly.ScreenState
import com.crewly.app.RxModule
import com.crewly.logging.LoggingManager
import com.crewly.roster.RosterManager
import com.crewly.roster.RosterPeriod
import com.crewly.roster.RosterRepository
import com.crewly.utils.plus
import com.crewly.viewmodel.ScreenStateViewModel
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 04/08/2018
 */
class RosterListViewModel @Inject constructor(application: Application,
                                              private val loggingManager: LoggingManager,
                                              private val rosterManager: RosterManager,
                                              private val rosterRepository: RosterRepository,
                                              @Named(RxModule.IO_THREAD) private val ioThread: Scheduler):
        AndroidViewModel(application), ScreenStateViewModel {

    override val screenState = BehaviorSubject.create<ScreenState>()
    private val rosterMonthsSubject = BehaviorSubject.create<List<RosterPeriod.RosterMonth>>()

    private val rosterMonths = mutableListOf<RosterPeriod.RosterMonth>()
    private val disposables = CompositeDisposable()

    init {
        fetchRoster()
        observeRosterUpdates()
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    fun observeRosterMonths(): Observable<List<RosterPeriod.RosterMonth>> =
            rosterMonthsSubject.hide()

    private fun observeRosterUpdates() {
        disposables + rosterManager
                .observeRosterUpdates()
                .subscribe { fetchRoster() }
    }

    private fun fetchRoster() {
        val months = mutableListOf<DateTime>()
        val monthStartTime = DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay()
        months.add(monthStartTime)

        for (i in 1 until 13) {
            val nextMonth = monthStartTime.plusMonths(i)
            months.add(nextMonth)
        }

        fetchMonthsInOrder(months)
    }

    private fun fetchMonthsInOrder(months: MutableList<DateTime>) {

        if (months.isNotEmpty()) {
            var fetchMonthsObservable = rosterRepository
                    .fetchRosterMonth(months[0])
                    .toObservable()

            for (i in 1 until months.size) {
                fetchMonthsObservable = fetchMonthsObservable
                        .concatWith(rosterRepository.fetchRosterMonth(months[i]))
            }

            disposables + fetchMonthsObservable
                    .subscribeOn(ioThread)
                    .doOnSubscribe {
                        rosterMonths.clear()
                        screenState.onNext(ScreenState.Loading(ScreenState.Loading.LOADING_ROSTER))
                    }
                    .subscribe ({ rosterMonth ->
                        if (rosterMonth.rosterDates.isNotEmpty()) {
                            rosterMonths.add(rosterMonth)
                        }
                    }, { error ->
                        loggingManager.logError(error, false)
                        screenState.onNext(ScreenState.Error())
                    }, {
                        rosterMonthsSubject.onNext(rosterMonths)
                        screenState.onNext(ScreenState.Success)
                    })
        }
    }
}