package com.crewly.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.R
import com.crewly.app.RxModule
import com.crewly.db.account.Account
import com.crewly.db.salary.Salary
import com.crewly.logging.LoggingManager
import com.crewly.models.Rank
import com.crewly.models.ScreenState
import com.crewly.roster.RosterHelper
import com.crewly.utils.plus
import com.crewly.viewmodel.ScreenStateViewModel
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 17/06/2018
 */
class AccountViewModel @Inject constructor(
  private val app: Application,
  private val accountManager: AccountManager,
  private val loggingManager: LoggingManager,
  private val rosterHelper: RosterHelper,
  @Named(RxModule.IO_THREAD) private val ioThread: Scheduler
):
  AndroidViewModel(app), ScreenStateViewModel {

  private val rankSelectionEvent = PublishSubject.create<Account>()
  private val salarySelectionEvent = PublishSubject.create<Account>()
  private val disposables = CompositeDisposable()

  override val screenState = BehaviorSubject.create<ScreenState>()

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeAccount(): Observable<Account> = accountManager.observeCurrentAccount()
  fun observeRankSelectionEvents(): Observable<Account> = rankSelectionEvent.hide()
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

  fun handleRankSelection() {
    rankSelectionEvent.onNext(accountManager.getCurrentAccount())
  }

  fun handleSalarySelection() {
    salarySelectionEvent.onNext(accountManager.getCurrentAccount())
  }

  fun deleteUserData() {
    disposables + rosterHelper
      .clearUserRosterDataFromNetwork(
        crewCode = accountManager.getCurrentAccount().crewCode
      )
      .andThen(
        accountManager.deleteAccount(
          account = accountManager.getCurrentAccount()
        )
      )
      .subscribeOn(ioThread)
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
      .subscribeOn(ioThread)
      .subscribe({}, { error ->
        loggingManager.logError(error)
      })
  }
}