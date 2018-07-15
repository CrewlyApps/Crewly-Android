package com.crewly.roster.list

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.paging.PagedList
import android.arch.paging.RxPagedListBuilder
import com.crewly.ScreenState
import com.crewly.app.CrewlyDatabase
import com.crewly.app.RxModule
import com.crewly.roster.RosterMonthDataSourceFactory
import com.crewly.roster.RosterPeriod
import com.crewly.utils.plus
import com.crewly.viewmodel.ScreenStateViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 27/05/2018
 */
class RosterListViewModel @Inject constructor(application: Application,
                                              crewlyDatabase: CrewlyDatabase,
                                              @Named(RxModule.IO_THREAD) private val ioThread: Scheduler):
        AndroidViewModel(application), ScreenStateViewModel {

    private val disposables = CompositeDisposable()

    private val rosterPagedList: Flowable<PagedList<RosterPeriod.RosterMonth>>
    private val roster = BehaviorSubject.create<PagedList<RosterPeriod.RosterMonth>>()
    override val screenState = BehaviorSubject.create<ScreenState>()

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    init {
        val config = PagedList.Config.Builder()
                .setPageSize(1)
                .setInitialLoadSizeHint(3)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        val rosterDataSourceFactory = RosterMonthDataSourceFactory(crewlyDatabase, disposables)
        val rosterPagedList = RxPagedListBuilder(rosterDataSourceFactory, config)
        val monthStartTime = DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay()
        rosterPagedList.setInitialLoadKey(monthStartTime)
        this.rosterPagedList = rosterPagedList.buildFlowable(BackpressureStrategy.LATEST)
    }

    fun observeRoster(): Observable<PagedList<RosterPeriod.RosterMonth>> {
        if (!roster.hasValue() && screenState.value !is ScreenState.Loading) { fetchRoster() }
        return roster.hide()
    }

    fun fetchRoster() {
        disposables + rosterPagedList
                .subscribeOn(ioThread)
                .doOnSubscribe { screenState.onNext(ScreenState.Loading(ScreenState.Loading.LOADING_ROSTER)) }
                .subscribe({ roster ->
                    this.roster.onNext(roster)
                    screenState.onNext(ScreenState.Success)
                }, { error ->
                    if (error is IOException) {
                        screenState.onNext(ScreenState.NetworkError)
                    } else {
                        screenState.onNext(ScreenState.Error())
                    }
                })
    }
}