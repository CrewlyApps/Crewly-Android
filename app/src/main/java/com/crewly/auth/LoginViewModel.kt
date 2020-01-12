package com.crewly.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.account.AccountManager
import com.crewly.models.Company
import com.crewly.views.ScreenState
import com.crewly.models.account.Account
import com.crewly.models.account.CrewType
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

  private val disposables = CompositeDisposable()

  override val screenState = BehaviorSubject.create<ScreenState>()
  private val company = BehaviorSubject.createDefault<Company>(Company.Norwegian)
  private val crewType = BehaviorSubject.createDefault<CrewType>(CrewType.CABIN)
  private val name = BehaviorSubject.create<String>()
  private val crewCode = BehaviorSubject.create<String>()
  private val password = BehaviorSubject.create<String>()

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeCompany(): Observable<Company> = company.hide()
  fun observeCrewType(): Observable<CrewType> = crewType.hide()
  fun observeName(): Observable<String> = name.hide()
  fun observeCrewCode(): Observable<String> = crewCode.hide()
  fun observePassword(): Observable<String> = password.hide()

  fun handleCrewTypeChange(
    crewType: CrewType
  ) {
    if (this.crewType.value == crewType) return
    this.crewType.onNext(crewType)
  }

  fun handleNameChange(
    name: String
  ) {
    if (this.name.value == name) return
    this.name.onNext(name)
  }

  fun handleCrewCodeChange(
    crewCode: String
  ) {
    if (this.crewCode.value == crewCode) return
    this.crewCode.onNext(crewCode)
  }

  fun handlePasswordChange(
    password: String
  ) {
    if (this.password.value == password) return
    this.password.onNext(password)
  }

  fun handleRequestRosterAttempt() {
    val name = this.name.value ?: ""
    val password = this.password.value ?: ""
    val validName = name.isNotBlank()
    val validPassword = password.isNotBlank()

    when {
      validName && validPassword -> {
        disposables + rosterRepository.fetchRoster(
          username = name,
          password = password,
          companyId = Company.Norwegian.id
        )
          .doOnSubscribe { screenState.onNext(ScreenState.Loading()) }
          .andThen(
            accountManager.createAccount(
              account = Account(
                crewCode = name,
                name = name,
                company = Company.Norwegian
              ),
              password = password
            )
          )
          .subscribeOn(Schedulers.io())
          .subscribe({
            screenState.onNext(ScreenState.Success)
          }, { error ->
            screenState.onNext(ScreenState.Error(error.message ?: ""))
          })
      }

      !validName && !validPassword -> screenState.onNext(ScreenState.Error("Please enter a username and password"))
      !validName -> screenState.onNext(ScreenState.Error("Please enter a username"))
      !validPassword -> screenState.onNext(ScreenState.Error("Please enter a password"))
    }
  }
}