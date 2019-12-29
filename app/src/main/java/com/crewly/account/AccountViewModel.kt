package com.crewly.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.R
import com.crewly.models.Salary
import com.crewly.logging.LoggingManager
import com.crewly.models.Rank
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
import javax.inject.Inject

/**
 * Created by Derek on 17/06/2018
 */
class AccountViewModel @Inject constructor(
  private val app: Application,
  private val accountManager: AccountManager,
  private val loggingManager: LoggingManager
):
  AndroidViewModel(app), ScreenStateViewModel {

  private val salarySelectionEvent = PublishSubject.create<Account>()
  private val disposables = CompositeDisposable()

  override val screenState = BehaviorSubject.create<ScreenState>()

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeAccount(): Observable<Account> = accountManager.observeCurrentAccount()
  fun observeSalarySelectionEvents(): Observable<Account> = salarySelectionEvent.hide()

  fun saveJoinedCompanyDate(joinedDate: DateTime) {
    val account = accountManager.getCurrentAccount()
    if (account.joinedCompanyAt != joinedDate) {
      updateAccount(account.copy(
        joinedCompanyAt = joinedDate
      ))
    }
  }

  fun saveShowCrew(showCrew: Boolean) {
    val account = accountManager.getCurrentAccount()
    if (account.showCrew != showCrew) {
      updateAccount(account.copy(
        showCrew = showCrew
      ))
    }
  }

  fun saveRank(rank: Rank) {
    val account = accountManager.getCurrentAccount()
    if (account.rank != rank) {
      updateAccount(account.copy(
        rank = rank
      ))
    }
  }

  fun saveSalary(salary: Salary) {
    val account = accountManager.getCurrentAccount()
    if (account.salary != salary) {
      updateAccount(account.copy(
        salary = salary
      ))
    }
  }

  fun handleSalarySelection() {
    salarySelectionEvent.onNext(accountManager.getCurrentAccount())
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
        ScreenState.Error(app.getString(R.string.account_delete_data_error))
        loggingManager.logError(error)
      }
  }

  private fun updateAccount(account: Account) {
    disposables + accountManager
      .updateAccount(account)
      .subscribeOn(Schedulers.io())
      .subscribe({}, { error ->
        loggingManager.logError(error)
      })
  }
}