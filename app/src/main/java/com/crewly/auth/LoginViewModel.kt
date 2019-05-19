package com.crewly.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.R
import com.crewly.ScreenState
import com.crewly.account.Account
import com.crewly.account.AccountManager
import com.crewly.app.RxModule
import com.crewly.logging.CrashlyticsManager
import com.crewly.logging.LoggingFlow
import com.crewly.logging.LoggingManager
import com.crewly.roster.Roster
import com.crewly.roster.RosterHelper
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
  private val app: Application,
  private val accountManager: AccountManager,
  private val rosterManager: RosterManager,
  private val rosterHelper: RosterHelper,
  private val crashlyticsManager: CrashlyticsManager,
  private val loggingManager: LoggingManager,
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

  fun saveRoster(
    roster: Roster
  ) {
    disposables + rosterHelper
      .saveRoster(
        crewCode = account?.crewCode ?: "",
        roster = roster
      )
      .toSingle { roster }
      .doOnEvent { _, _ -> rosterManager.rosterUpdated() }
      .flatMapCompletable { Completable.defer { saveAccount() } }
      .subscribeOn(ioThread)
      .subscribe({
        updateScreenState(ScreenState.Success)
      }) { error ->
        loggingManager.logError(error)
        updateScreenState(ScreenState.Error(
          errorMessage = app.getString(R.string.login_error_saving_roster)
        ))
      }
  }

  fun createAccount(): Completable =
    account?.let { account ->
      accountManager.createAccount(account)
    } ?: Completable.error(Throwable("Account not created"))

  fun updateIsPilot(isPilot: Boolean) {
    account?.isPilot = isPilot
    crashlyticsManager.addLoggingKey(CrashlyticsManager.IS_PILOT_KEY, isPilot)
  }

  fun saveAccount(): Completable =
    account?.let { account ->
      loggingManager.logMessage(LoggingFlow.ACCOUNT, "Save account")
      val newAccount = account.copy(
        crewCode = userName
      )

      accountManager
        .updateAccount(newAccount)
        .ignoreElement()
    } ?: Completable.error(Throwable("Account not created"))

  private fun fetchAccount() {
    disposables + accountManager
      .getAccount(userName)
      .subscribeOn(ioThread)
      .subscribe { account -> this.account = account }
  }
}