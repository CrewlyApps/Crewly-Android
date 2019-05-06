package com.crewly.account

import android.annotation.SuppressLint
import com.crewly.app.CrewlyDatabase
import com.crewly.app.CrewlyPreferences
import com.crewly.app.RxModule
import com.crewly.logging.LoggingFlow
import com.crewly.logging.LoggingManager
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by Derek on 30/06/2018
 */
@Singleton
@SuppressLint("CheckResult")
class AccountManager @Inject constructor(
  private val crewlyPreferences: CrewlyPreferences,
  private val loggingManager: LoggingManager,
  private val crewlyDatabase: CrewlyDatabase,
  @Named(RxModule.IO_THREAD) private val ioThread: Scheduler
) {

  private val currentAccount = BehaviorSubject.createDefault(Account())
  private val currentAccountSwitchEvent = PublishSubject.create<Account>()

  private var monitorCurrentAccountDisposable: Disposable? = null

   init {
     monitorCurrentAccount()
  }

  fun getCurrentAccount(): Account = currentAccount.value ?: Account()

  fun switchCurrentAccount(account: Account) {
    val currentAccount = getCurrentAccount()
    if (currentAccount.crewCode != account.crewCode) {
      loggingManager.logMessage(LoggingFlow.ACCOUNT, "Current Account Switched, code = ${account.crewCode}")
      crewlyPreferences.saveCurrentAccount(account.crewCode)
      currentAccountSwitchEvent.onNext(account)
      monitorCurrentAccount()
    }
  }

  /**
   * Observe any account changes. Will emit events whenever the current account is switched to
   * another account.
   */
  fun observeAccountSwitchEvents(): Observable<Account> = currentAccountSwitchEvent.hide()

  /**
   * Observe the current account. Will emit events whenever the data in the current account
   * changes.
   */
  fun observeCurrentAccount(): Flowable<Account> {
    val crewCode = crewlyPreferences.getCurrentAccount()
    return crewlyDatabase.accountDao()
      .observeAccount(crewCode)
      .map { accounts -> if (accounts.isNotEmpty()) accounts[0] else Account() }
      .subscribeOn(ioThread)
  }

  private fun monitorCurrentAccount() {
    monitorCurrentAccountDisposable?.dispose()
    monitorCurrentAccountDisposable = observeCurrentAccount()
      .subscribe { account ->
        loggingManager.logMessage(LoggingFlow.ACCOUNT, "Current Account Update, code = ${account.crewCode}")
        currentAccount.onNext(account)
      }
  }
}