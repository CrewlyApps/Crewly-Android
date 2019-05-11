package com.crewly.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.app.RxModule
import com.crewly.crew.Rank
import com.crewly.logging.LoggingManager
import com.crewly.salary.Salary
import com.crewly.utils.plus
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 17/06/2018
 */
class AccountViewModel @Inject constructor(
  app: Application,
  private val accountManager: AccountManager,
  private val loggingManager: LoggingManager,
  @Named(RxModule.IO_THREAD) private val ioThread: Scheduler
):
  AndroidViewModel(app) {

  private val rankSelectionEvent = PublishSubject.create<Account>()
  private val salarySelectionEvent = PublishSubject.create<Account>()
  private val disposables = CompositeDisposable()

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeAccount(): Flowable<Account> = accountManager.observeCurrentAccount()
  fun observeRankSelectionEvents(): Observable<Account> = rankSelectionEvent.hide()
  fun observeSalarySelectionEvents(): Observable<Account> = salarySelectionEvent.hide()

  /**
   * Save [joinedDate] to the user's account in the database.
   */
  fun saveJoinedCompanyDate(joinedDate: DateTime) {
    val account = accountManager.getCurrentAccount()
    if (account.joinedCompanyAt != joinedDate) {
      updateAccount(account.copy(
        joinedCompanyAt = joinedDate
      ))
    }
  }

  /**
   * Save [rank] to the user's account in the database.
   */
  fun saveRank(rank: Rank) {
    val account = accountManager.getCurrentAccount()
    if (account.rank != rank) {
      updateAccount(account.copy(
        rank = rank
      ))
    }
  }

  /**
   * Save [salary] in the user's account in the database.
   */
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

  private fun updateAccount(account: Account) {
    disposables + accountManager
      .updateAccount(account)
      .subscribeOn(ioThread)
      .subscribe({}, { error ->
        loggingManager.logError(error)
      })
  }
}