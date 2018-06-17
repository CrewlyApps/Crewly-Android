package com.crewly.account

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.crewly.app.CrewlyDatabase
import com.crewly.app.CrewlyPreferences
import com.crewly.app.RxModule
import com.crewly.auth.Account
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 17/06/2018
 */
class AccountViewModel @Inject constructor(app: Application,
                                           private val crewlyPreferences: CrewlyPreferences,
                                           private val crewlyDatabase: CrewlyDatabase,
                                           @Named(RxModule.IO_THREAD) private val ioThread: Scheduler):
        AndroidViewModel(app) {

    private val disposables = CompositeDisposable()

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    fun observeAccount(): Observable<Account> {
        val crewCode = crewlyPreferences.getCurrentAccount()
        return crewlyDatabase.accountDao()
                .fetchAccount(crewCode)
                .map { accounts -> if (accounts.isNotEmpty()) accounts[0] else Account() }
                .subscribeOn(ioThread)
                .toObservable()
    }
}