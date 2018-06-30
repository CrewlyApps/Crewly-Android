package com.crewly.account

import com.crewly.app.CrewlyDatabase
import com.crewly.app.CrewlyPreferences
import com.crewly.app.RxModule
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 30/06/2018
 */
class AccountManager @Inject constructor(private val crewlyPreferences: CrewlyPreferences,
                                         private val crewlyDatabase: CrewlyDatabase,
                                         @Named(RxModule.IO_THREAD) private val ioThread: Scheduler) {

    private val currentAccountSubject = BehaviorSubject.create<Account>()

    init { fetchCurrentAccount() }

    fun getCurrentAccount(): Account =
        currentAccountSubject.value ?: Account()

    fun switchCurrentAccount(account: Account) {
        val currentAccount = currentAccountSubject.value

        if (currentAccount == null || currentAccount.crewCode != account.crewCode) {
            crewlyPreferences.saveCurrentAccount(account.crewCode)
            currentAccountSubject.onNext(account)
        }
    }

    /**
     * Observe any account changes. Will emit events whenever the current account is switched to
     * another account.
     */
    fun observeAccount(): Observable<Account> =
            currentAccountSubject
                    .skip(1)
                    .hide()

    /**
     * Observe the current account. Will emit events whenever the data in the current account
     * changes.
     */
    fun observeCurrentAccount(): Flowable<Account> {
        val crewCode = crewlyPreferences.getCurrentAccount()
        return crewlyDatabase.accountDao()
                .fetchAccount(crewCode)
                .map { accounts -> if (accounts.isNotEmpty()) accounts[0] else Account() }
                .subscribeOn(ioThread)
    }

    private fun fetchCurrentAccount() {
        val crewCode = crewlyPreferences.getCurrentAccount()
        crewlyDatabase.accountDao()
                .fetchAccount(crewCode)
                .map { accounts -> if (accounts.isNotEmpty()) accounts[0] else Account() }
                .subscribeOn(ioThread)
                .subscribe { account -> currentAccountSubject.onNext(account) }
    }
}