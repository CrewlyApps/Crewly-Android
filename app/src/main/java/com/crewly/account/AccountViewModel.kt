package com.crewly.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.R
import com.crewly.models.FutureDaysPattern
import com.crewly.models.Salary
import com.crewly.views.ScreenState
import com.crewly.models.account.Account
import com.crewly.utils.plus
import com.crewly.viewmodel.ScreenStateViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Derek on 17/06/2018
 */
class AccountViewModel @Inject constructor(
  private val app: Application,
  private val accountManager: AccountManager
):
  AndroidViewModel(app), ScreenStateViewModel {

  private val salarySelectionEvents = PublishSubject.create<Salary>()
  private val futureDaysPatternSelectionEvents = PublishSubject.create<FutureDaysPattern>()
  private val disposables = CompositeDisposable()

  override val screenState = BehaviorSubject.create<ScreenState>()

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeAccount(): Observable<Account> = accountManager.observeCurrentAccount()
  fun observeSalarySelectionEvents(): Observable<Salary> = salarySelectionEvents.hide()
  fun observeFutureDaysSelectionEvents(): Observable<FutureDaysPattern> = futureDaysPatternSelectionEvents.hide()

  fun saveJoinedCompanyDate(
    joinedDate: DateTime
  ) {
    val account = accountManager.getCurrentAccount()
    if (account.joinedCompanyAt != joinedDate) {
      updateAccount(account.copy(
        joinedCompanyAt = joinedDate
      ))
    }
  }

  fun saveSalary(
    salary: Salary
  ) {
    val account = accountManager.getCurrentAccount()
    if (account.salary != salary) {
      updateAccount(account.copy(
        salary = salary
      ))
    }
  }

  fun saveFutureDaysPattern(
    pattern: FutureDaysPattern
  ) {
    val account = accountManager.getCurrentAccount()
    if (account.futureDaysPattern != pattern) {
      updateAccount(
        account.copy(
          futureDaysPattern = pattern
        )
      )
    }
  }

  fun handleSalarySelection() {
    salarySelectionEvents.onNext(accountManager.getCurrentAccount().salary)
  }

  fun handleFutureDaysPatternSelection() {
    futureDaysPatternSelectionEvents.onNext(accountManager.getCurrentAccount().futureDaysPattern)
  }

  fun deleteUserData() {
    disposables + accountManager.deleteAccount(
      account = accountManager.getCurrentAccount()
    )
      .subscribeOn(Schedulers.io())
      .doOnSubscribe { screenState.onNext(ScreenState.Loading()) }
      .subscribe({
        screenState.onNext(ScreenState.Success)
      }) { error ->
        Timber.e(error)
        ScreenState.Error(app.getString(R.string.account_delete_data_error))
      }
  }

  private fun updateAccount(
    account: Account
  ) {
    disposables + accountManager
      .updateAccount(account)
      .subscribeOn(Schedulers.io())
      .subscribe({}, { error -> Timber.e(error) })
  }
}