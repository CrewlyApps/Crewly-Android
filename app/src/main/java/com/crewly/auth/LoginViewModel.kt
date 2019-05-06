package com.crewly.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.ScreenState
import com.crewly.account.Account
import com.crewly.account.AccountManager
import com.crewly.app.CrewlyDatabase
import com.crewly.app.RxModule
import com.crewly.logging.CrashlyticsManager
import com.crewly.roster.RosterManager
import com.crewly.utils.plus
import com.crewly.viewmodel.ScreenStateViewModel
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 10/06/2018
 */
class LoginViewModel @Inject constructor(
  app: Application,
  private val crewlyDatabase: CrewlyDatabase,
  private val accountManager: AccountManager,
  private val rosterManager: RosterManager,
  private val crashlyticsManager: CrashlyticsManager,
  @Named(RxModule.IO_THREAD) private val ioThread: Scheduler
):
  AndroidViewModel(app), ScreenStateViewModel {

  private val disposables = CompositeDisposable()

  override val screenState = BehaviorSubject.create<ScreenState>()

  var serviceType: ServiceType = ServiceType.RYANAIR
  private set

  var account: Account? = null
  private set

  var userName: String = ""
  private set

  var password: String = ""
  private set

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun handleUserNameChange(userName: String) {
    this.userName = userName.trim()
  }

  fun handlePasswordChange(password: String) {
    this.password = password
  }

  fun handleLoginAttempt() {
    val validUserName = userName.isNotBlank()
    val validPassword = password.isNotBlank()

    when {
      validUserName && validPassword -> {
        fetchAccount()
        screenState.onNext(ScreenState.Loading(ScreenState.Loading.LOGGING_IN))
      }

      !validUserName && !validPassword -> screenState.onNext(ScreenState.Error("Please enter a username and password"))
      !validUserName -> screenState.onNext(ScreenState.Error("Please enter a username"))
      !validPassword -> screenState.onNext(ScreenState.Error("Please enter a password"))
    }
  }

  fun rosterUpdated() = rosterManager.rosterUpdated()

  fun createAccount(): Completable {
    return Completable.fromAction {
      crewlyDatabase.accountDao().insertAccount(account!!)
    }
  }

  fun updateIsPilot(isPilot: Boolean) {
    account?.isPilot = isPilot
    crashlyticsManager.addLoggingKey(CrashlyticsManager.IS_PILOT_KEY, isPilot)
  }

  fun saveAccount(): Completable {
    return Completable
      .fromAction {
        account?.let {
          it.crewCode = userName
          crewlyDatabase.accountDao().updateAccount(it)
          accountManager.switchCurrentAccount(it)
        }
      }
  }

  private fun fetchAccount() {
    disposables + crewlyDatabase.accountDao()
      .fetchAccount(userName)
      .subscribeOn(ioThread)
      .map { accounts -> if (accounts.isNotEmpty()) accounts[0] else Account(userName) }
      .subscribe { account -> this.account = account }
  }
}