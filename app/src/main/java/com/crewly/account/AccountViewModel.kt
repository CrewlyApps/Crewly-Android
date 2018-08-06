package com.crewly.account

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.crewly.app.CrewlyDatabase
import com.crewly.app.CrewlyPreferences
import com.crewly.app.RxModule
import com.crewly.crew.Rank
import com.crewly.roster.RosterManager
import com.crewly.salary.Salary
import com.crewly.utils.plus
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 17/06/2018
 */
class AccountViewModel @Inject constructor(app: Application,
                                           private val crewlyPreferences: CrewlyPreferences,
                                           private val crewlyDatabase: CrewlyDatabase,
                                           private val accountManager: AccountManager,
                                           private val rosterManager: RosterManager,
                                           @Named(RxModule.IO_THREAD) private val ioThread: Scheduler):
        AndroidViewModel(app) {

    private val disposables = CompositeDisposable()

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    fun processDeleteDataClicks(clicks: Flowable<Unit>): Flowable<Unit> {
        return clicks
                .doOnNext {
                    disposables + Completable.fromAction { crewlyDatabase.clearAllTables()  }
                            .subscribeOn(ioThread)
                            .subscribe {
                                crewlyPreferences.clearPreferences()
                                rosterManager.observeRosterUpdates()
                            }
                }
    }

    fun observeAccount(): Flowable<Account> =
            accountManager.observeCurrentAccount()

    fun getAccount(): Account =
            accountManager.getCurrentAccount()

    /**
     * Save [joinedDate] to the user's account in the database.
     */
    fun saveJoinedCompanyDate(joinedDate: DateTime) {
        val account = accountManager.getCurrentAccount()
        if (account.joinedCompanyAt != joinedDate) {
            account.joinedCompanyAt = joinedDate
            updateAccount(account)
        }
    }

    /**
     * Save [rank] to the user's account in the database.
     */
    fun saveRank(rank: Rank) {
        val account = accountManager.getCurrentAccount()
        if (account.rank != rank) {
            account.rank = rank
            updateAccount(account)
        }
    }

    /**
     * Save [salary] in the user's account in the database.
     */
    fun saveSalary(salary: Salary) {
        val account = accountManager.getCurrentAccount()
        if (account.salary != salary) {
            account.salary = salary
            updateAccount(account)
        }
    }

    private fun updateAccount(account: Account) {
        disposables + Completable.fromAction { crewlyDatabase.accountDao().updateAccount(account) }
                .subscribeOn(ioThread)
                .subscribe {}
    }
}