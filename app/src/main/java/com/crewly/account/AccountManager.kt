package com.crewly.account

import android.annotation.SuppressLint
import com.crewly.BuildConfig
import com.crewly.app.CrewlyPreferences
import com.crewly.app.RxModule
import com.crewly.aws.AwsRepository
import com.crewly.db.CrewlyDatabase
import com.crewly.db.account.Account
import com.crewly.logging.LoggingFlow
import com.crewly.logging.LoggingManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
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
  private val accountRepository: AccountRepository,
  private val awsRepository: AwsRepository,
  @Named(RxModule.IO_THREAD) private val ioThread: Scheduler
) {

  private val currentAccount = BehaviorSubject.createDefault(Account())
  private val currentAccountSwitchEvent = PublishSubject.create<Account>()

  private var monitorCurrentAccountDisposable: Disposable? = null

   init {
     monitorCurrentAccount()
  }

  fun getCurrentAccount(): Account = currentAccount.value ?: Account()

  fun getPassword(crewCode: String): Single<String> = accountRepository.getPassword(crewCode)

  fun getAccount(crewCode: String): Single<Account> =
    accountRepository
      .getAccount(
        id = crewCode
      )

  fun updateAccount(account: Account): Single<Account> =
    accountRepository
      .updateAccount(account)
      .toSingle { account }
      .doOnSuccess {
        if (getCurrentAccount().crewCode != account.crewCode) {
          switchCurrentAccount(account)
        }

        updateAwsAccount(account)
      }

  fun updateAccount(
    account: Account,
    password: String
  ): Single<Account> =
    Single.zip(
      updateAccount(account),
      accountRepository.savePassword(
        crewCode = account.crewCode,
        password = password
      ).toSingle { Unit }, BiFunction { updatedAccount, _ -> updatedAccount }
    )

  fun createAccount(account: Account): Completable =
    accountRepository
      .createAccount(
        account = account
      )

  fun deleteAccount(account: Account): Completable =
    if (BuildConfig.DEBUG) {
      Completable.complete()
    } else {
      awsRepository
        .deleteUser(
          userId = account.crewCode,
          companyId = account.company.id
        )
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
  fun observeCurrentAccount(): Observable<Account> = currentAccount.hide()

  private fun monitorCurrentAccount() {
    val crewCode = crewlyPreferences.getCurrentAccount()
    monitorCurrentAccountDisposable?.dispose()
    monitorCurrentAccountDisposable = crewlyDatabase.accountDao()
      .observeAccount(crewCode)
      .map { accounts -> if (accounts.isNotEmpty()) accounts[0] else Account() }
      .subscribeOn(ioThread)
      .subscribe({ account ->
        if (getCurrentAccount() != account) {
          loggingManager.logMessage(LoggingFlow.ACCOUNT, "Current Account Update, code = ${account.crewCode}")
          currentAccount.onNext(account)
        }
      }, { error -> loggingManager.logError(error) })
  }

  private fun switchCurrentAccount(account: Account) {
    val currentAccount = getCurrentAccount()
    if (currentAccount.crewCode != account.crewCode) {
      loggingManager.logMessage(LoggingFlow.ACCOUNT, "Current Account Switched, code = ${account.crewCode}")
      crewlyPreferences.saveCurrentAccount(account.crewCode)
      this.currentAccount.onNext(account)
      currentAccountSwitchEvent.onNext(account)
      monitorCurrentAccount()
    }
  }

  private fun updateAwsAccount(
    account: Account
  ) {
    if (BuildConfig.DEBUG) return

    awsRepository
      .createOrUpdateUser(account)
      .subscribeOn(ioThread)
      .subscribe({}, { error ->
        loggingManager.logError(error)
      })
  }
}