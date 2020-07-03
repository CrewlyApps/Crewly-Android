package com.crewly.account

import android.annotation.SuppressLint
import com.crewly.BuildConfig
import com.crewly.logging.Logger
import com.crewly.models.account.Account
import com.crewly.repositories.AccountRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Derek on 30/06/2018
 */
@Singleton
@SuppressLint("CheckResult")
class AccountManager @Inject constructor(
  private val accountRepository: AccountRepository
) {

  private val currentAccount = BehaviorSubject.createDefault(Account())
  private val currentAccountSwitchEvent = PublishSubject.create<Account>()

  private var monitorCurrentAccountDisposable: Disposable? = null

   init {
     monitorCurrentAccount()
  }

  fun getCurrentAccount(): Account = currentAccount.value ?: Account()

  fun getPassword(
    crewCode: String
  ): Single<String> =
    accountRepository.getPassword(crewCode)

  fun savePassword(
    password: String
  ): Completable =
    accountRepository.savePassword(
      crewCode = currentAccount.value?.crewCode ?: "",
      password = password
    )

  fun getAccount(
    crewCode: String
  ): Single<Account> =
    accountRepository
      .getAccount(
        id = crewCode
      )

  fun updateAccount(
    account: Account
  ): Single<Account> =
    accountRepository
      .updateAccount(account)
      .toSingle { account }

  fun createAccount(
    account: Account,
    password: String
  ): Completable =
    Completable.mergeArray(
      accountRepository
        .createOrReplaceAccount(
          account = account
        ),
      accountRepository.savePassword(
        crewCode = account.crewCode,
        password = password
      )
    )
      .doOnComplete {
        if (getCurrentAccount().crewCode != account.crewCode) {
          switchCurrentAccount(account)
        }
      }

  fun deleteAccount(
    account: Account
  ): Completable =
    if (BuildConfig.DEBUG) {
      Completable.complete()
    } else {
      Completable.mergeArray(
        accountRepository.clearCurrentCrewCode(),
        accountRepository.clearPassword(
          crewCode = account.crewCode
        )
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
    monitorCurrentAccountDisposable?.dispose()
    monitorCurrentAccountDisposable = accountRepository
      .getCurrentCrewCode()
      .flatMapPublisher { crewCode ->
        accountRepository.observeAccount(
          crewCode = crewCode
        )
      }
      .subscribeOn(Schedulers.io())
      .subscribe({ account ->
        if (getCurrentAccount() != account) {
          Logger.logDebug("Current Account Update, code = ${account.crewCode}")
          currentAccount.onNext(account)
        }
      }, { error -> Logger.logError(error) })
  }

  private fun switchCurrentAccount(
    account: Account
  ) {
    val currentAccount = getCurrentAccount()
    if (currentAccount.crewCode != account.crewCode) {
      Logger.logDebug("Current Account Switched, code = ${account.crewCode}")

      accountRepository.saveCurrentCrewCode(
        crewCode = account.crewCode
      )
        .subscribeOn(Schedulers.io())
        .subscribe({
          this.currentAccount.onNext(account)
          currentAccountSwitchEvent.onNext(account)
          monitorCurrentAccount()
        }) { error -> Logger.logError(error) }
    }
  }
}