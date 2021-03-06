package com.crewly.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.account.AccountManager
import com.crewly.logging.Logger
import com.crewly.models.Company
import com.crewly.views.ScreenState
import com.crewly.models.account.Account
import com.crewly.models.account.CrewType
import com.crewly.models.roster.future.FutureDaysPattern
import com.crewly.repositories.FetchRosterUseCase
import com.crewly.repositories.LoginDisplayDataUseCase
import com.crewly.utils.plus
import com.crewly.viewmodel.ScreenStateViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Created by Derek on 10/06/2018
 */
class LoginViewModel @Inject constructor(
  app: Application,
  private val accountManager: AccountManager,
  private val fetchRosterUseCase: FetchRosterUseCase,
  private val loginDisplayDataUseCase: LoginDisplayDataUseCase
):
  AndroidViewModel(app), ScreenStateViewModel {

  private val disposables = CompositeDisposable()

  override val screenState = BehaviorSubject.create<ScreenState>()
  private val company = BehaviorSubject.createDefault<Company>(Company.Ryanair)
  private val crewType = BehaviorSubject.createDefault(CrewType.CABIN)
  private val name = BehaviorSubject.create<String>()
  private val crewCode = BehaviorSubject.create<String>()
  private val password = BehaviorSubject.create<String>()
  private val showWarningMessageEvents = PublishSubject.create<String>()

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeCompany(): Observable<Company> = company.hide()
  fun observeCrewType(): Observable<CrewType> = crewType.hide()
  fun observeName(): Observable<String> = name.hide()
  fun observeCrewCode(): Observable<String> = crewCode.hide()
  fun observePassword(): Observable<String> = password.hide()
  fun observeShowWarningMessageEvents(): Observable<String> = showWarningMessageEvents.hide()

  fun handleCompanyChange(
    company: Company
  ) {
    if (this.company.value == company) return
    this.company.onNext(company)
  }

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
    val crewCode = this.crewCode.value ?: ""
    val password = this.password.value ?: ""
    val crewType = this.crewType.value ?: CrewType.CABIN
    val validName = name.isNotBlank()
    val validCrewCode = crewCode.isNotBlank()
    val validPassword = password.isNotBlank()

    when {
      validName && validCrewCode && validPassword -> {
        val company = this.company.value ?: Company.Ryanair
        disposables + fetchRosterUseCase.fetchRoster(
          username = crewCode,
          password = password,
          companyId = company.id,
          crewType = crewType
        )
          .doOnSubscribe { screenState.onNext(ScreenState.Loading()) }
          .flatMap { data ->
            accountManager.createAccount(
              account = Account(
                crewCode = crewCode,
                name = name,
                company = company,
                crewType = crewType.type,
                base = data.userBase,
                futureDaysPattern = when (crewType) {
                  CrewType.CABIN -> FutureDaysPattern().toCrewPattern()
                  CrewType.FLIGHT -> FutureDaysPattern().toPilotPattern()
                }
              ),
              password = password
            )
              .toSingle { data }
          }
          .subscribeOn(Schedulers.io())
          .subscribe({ data ->
            if (data.showFirstRyanairRosterFetchMessage) {
              showWarningMessageEvents.onNext(
                loginDisplayDataUseCase.getRyanairFirstRosterFetchWarningMessage()
              )
            } else {
              screenState.onNext(ScreenState.Success)
            }
          }, { error ->
            Logger.logError(error)
            val message = if (!error.message.isNullOrBlank()) {
              error.message ?: ""
            } else {
              "Failed to retrieve your roster. Please try again."
            }

            screenState.onNext(ScreenState.Error(message))
          })
      }

      else -> screenState.onNext(ScreenState.Error("Fields must not be empty"))
    }
  }

  fun handleWarningMessageDismissed() {
    screenState.onNext(ScreenState.Success)
  }
}