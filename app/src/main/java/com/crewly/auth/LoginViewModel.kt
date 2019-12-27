package com.crewly.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.R
import com.crewly.account.AccountManager
import com.crewly.persistence.account.Account
import com.crewly.logging.CrashlyticsManager
import com.crewly.logging.LoggingFlow
import com.crewly.logging.LoggingManager
import com.crewly.models.ScreenState
import com.crewly.models.WebServiceType
import com.crewly.models.roster.Roster
import com.crewly.roster.RosterHelper
import com.crewly.roster.RosterManager
import com.crewly.utils.plus
import com.crewly.viewmodel.ScreenStateViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by Derek on 10/06/2018
 */
class LoginViewModel @Inject constructor(
  private val app: Application,
  private val accountManager: AccountManager,
  private val rosterManager: RosterManager,
  private val rosterHelper: RosterHelper,
  private val crashlyticsManager: CrashlyticsManager,
  private val loggingManager: LoggingManager
):
  AndroidViewModel(app), ScreenStateViewModel {

  companion object {
    const val LOADING_LOGGING_IN = 1
    const val LOADING_FETCHING_ROSTER = 2
  }

  private val disposables = CompositeDisposable()

  override val screenState = BehaviorSubject.create<ScreenState>()
  private val userName = BehaviorSubject.create<String>()
  private val password = BehaviorSubject.create<String>()

  var webServiceType: WebServiceType = WebServiceType.CrewDock()
  private set

  var account: Account? = null
  private set

  init {
    val currentAccount = accountManager.getCurrentAccount()
    val crewCode = currentAccount.crewCode
    if (crewCode.isNotBlank()) {
      disposables + accountManager
        .getPassword(
          crewCode = crewCode
        )
        .subscribeOn(Schedulers.io())
        .subscribe({ password ->
          account = currentAccount
          userName.onNext(crewCode)
          this.password.onNext(password)
        }, { error -> loggingManager.logError(error) })
    }
  }

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeUserName(): Observable<String> = userName.hide()
  fun observePassword(): Observable<String> = password.hide()

  fun getUserName(): String = userName.value ?: ""
  fun getPassword(): String = password.value ?: ""

  fun handleUserNameChange(userName: String) {
    if (this.userName.value == userName) return
    this.userName.onNext(userName)
  }

  fun handlePasswordChange(password: String) {
    if (this.password.value == password) return
    this.password.onNext(password)
  }

  fun handleLoginAttempt() {
    val validUserName = userName.value?.isNotBlank() == true
    val validPassword = password.value?.isNotBlank() == true

    when {
      validUserName && validPassword -> {
        fetchAccount()
        screenState.onNext(ScreenState.Loading(
          id = LOADING_LOGGING_IN)
        )
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
      .subscribeOn(Schedulers.io())
      .subscribe({
        updateScreenState(ScreenState.Success)
      }) { error ->
        loggingManager.logError(error)
        updateScreenState(ScreenState.Error(
          message = app.getString(R.string.login_error_saving_roster)
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

  private fun saveAccount(): Completable =
    account?.let { account ->
      loggingManager.logMessage(LoggingFlow.ACCOUNT, "Save account")
      val newAccount = account.copy(
        crewCode = userName.value?.trim() ?: ""
      )

      accountManager
        .updateAccount(
          account = newAccount,
          password = password.value ?: ""
        )
        .ignoreElement()
    } ?: Completable.error(Throwable("Account not created"))

  private fun fetchAccount() {
    disposables + accountManager
      .getAccount(userName.value ?: "")
      .subscribeOn(Schedulers.io())
      .subscribe({ account -> this.account = account })
      { error -> loggingManager.logError(error)}
  }
}