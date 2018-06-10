package com.crewly.roster

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.crewly.ScreenState
import com.crewly.app.RxModule
import com.crewly.utils.plus
import com.crewly.viewmodel.ScreenStateViewModel
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 27/05/2018
 */
class RosterViewModel @Inject constructor(application: Application,
                                          private val rosterRepository: RosterRepository,
                                          @Named(RxModule.IO_THREAD) private val ioThread: Scheduler):
        AndroidViewModel(application), ScreenStateViewModel {

    private val disposables = CompositeDisposable()

    private val roster = BehaviorSubject.create<List<RosterPeriod.RosterMonth>>()
    override val screenState = BehaviorSubject.create<ScreenState>()

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    fun observeRoster(): Observable<List<RosterPeriod.RosterMonth>> {
        if (!roster.hasValue() && screenState.value != ScreenState.Loading) { fetchRoster() }
        return roster.hide()
    }

    fun fetchRoster() {
        disposables + rosterRepository
                .fetchRoster()
                .subscribeOn(ioThread)
                .doOnSubscribe { screenState.onNext(ScreenState.Loading) }
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