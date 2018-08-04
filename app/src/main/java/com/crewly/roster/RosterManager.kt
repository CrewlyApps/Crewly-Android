package com.crewly.roster

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Derek on 04/08/2018
 */
@Singleton
class RosterManager @Inject constructor() {

    private val rosterUpdatedSubject = PublishSubject.create<Unit>()

    fun observeRosterUpdates(): Observable<Unit> =
            rosterUpdatedSubject.hide()

    fun rosterUpdated() = rosterUpdatedSubject.onNext(Unit)
}