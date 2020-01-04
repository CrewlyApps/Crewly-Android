package com.crewly.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.account.AccountManager
import com.crewly.logging.LoggingManager
import com.crewly.models.Company
import com.crewly.views.ScreenState
import com.crewly.models.account.Account
import com.crewly.repositories.RosterRepository
import com.crewly.utils.plus
import com.crewly.viewmodel.ScreenStateViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by Derek on 10/06/2018
 */
class LoginViewModel @Inject constructor(
  app: Application,
  private val accountManager: AccountManager,
  private val rosterRepository: RosterRepository
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

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeUserName(): Observable<String> = userName.hide()
  fun observePassword(): Observable<String> = password.hide()

  fun handleUserNameChange(userName: String) {
    if (this.userName.value == userName) return
    this.userName.onNext(userName)
  }

  fun handlePasswordChange(password: String) {
    if (this.password.value == password) return
    this.password.onNext(password)
  }

  fun handleLoginAttempt() {
    val username = this.userName.value ?: ""
    val password = this.password.value ?: ""
    val validUserName = username.isNotBlank()
    val validPassword = password.isNotBlank()

    when {
      validUserName && validPassword -> {
        disposables + rosterRepository.fetchRoster(
          username = username,
          password = password,
          companyId = Company.Norwegian.id
        )
          .andThen {
            accountManager.createAccount(
              account = Account(
                crewCode = username,
                name = username,
                company = Company.Norwegian
              ),
              password = password
            )
          }
          .subscribeOn(Schedulers.io())
          .subscribe({
            screenState.onNext(ScreenState.Success)
          }, { error ->
            screenState.onNext(ScreenState.Error(error.message ?: ""))
          })
      }

      !validUserName && !validPassword -> screenState.onNext(ScreenState.Error("Please enter a username and password"))
      !validUserName -> screenState.onNext(ScreenState.Error("Please enter a username"))
      !validPassword -> screenState.onNext(ScreenState.Error("Please enter a password"))
    }
  }
}