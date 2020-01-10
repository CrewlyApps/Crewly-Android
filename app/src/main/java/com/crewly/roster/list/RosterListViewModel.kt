package com.crewly.roster.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.account.AccountManager
import com.crewly.logging.LoggingFlow
import com.crewly.logging.LoggingManager
import com.crewly.views.ScreenState
import com.crewly.models.account.Account
import com.crewly.models.roster.RosterPeriod
import com.crewly.roster.RosterManager
import com.crewly.repositories.RosterRepository
import com.crewly.utils.plus
import com.crewly.viewmodel.ScreenStateViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * Created by Derek on 04/08/2018
 */
class RosterListViewModel @Inject constructor(
  application: Application,
  private val accountManager: AccountManager,
  private val loggingManager: LoggingManager,
  private val rosterManager: RosterManager,
  private val rosterRepository: RosterRepository
):
  AndroidViewModel(application), ScreenStateViewModel {

  override val screenState = BehaviorSubject.create<ScreenState>()
  private val rosterMonthsSubject = BehaviorSubject.create<List<RosterPeriod.RosterMonth>>()

  private val rosterMonths = mutableListOf<RosterPeriod.RosterMonth>()
  private val disposables = CompositeDisposable()

  var showingEmptyView = false

  init {
    getRoster()
    observeRosterUpdates()
    observeAccountUpdates()
  }

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeRosterMonths(): Observable<List<RosterPeriod.RosterMonth>> =
    rosterMonthsSubject.hide()

  fun handleRefreshRoster() {
    val username = accountManager.getCurrentAccount().crewCode
    val companyId = accountManager.getCurrentAccount().company.id

    disposables + accountManager.getPassword(
      crewCode = username
    )
      .subscribeOn(Schedulers.io())
      .doOnSubscribe { screenState.onNext(ScreenState.Loading()) }
      .flatMapCompletable { password ->
        rosterRepository.fetchRoster(
          username = username,
          password = password,
          companyId = companyId
        )
      }
      .subscribe({
        getRoster()
      }, { error ->
        loggingManager.logError(error)
      })
  }

  private fun observeRosterUpdates() {
    disposables + rosterManager
      .observeRosterUpdates()
      .subscribe({
        if (accountManager.getCurrentAccount().crewCode.isNotEmpty()) {
          loggingManager.logMessage(LoggingFlow.ROSTER_LIST, "Roster Update Observed")
          getRoster()
        }
      }) { error -> loggingManager.logError(error) }
  }

  private fun observeAccountUpdates() {
    disposables + accountManager
      .observeAccountSwitchEvents()
      .subscribe({
        loggingManager.logMessage(LoggingFlow.ROSTER_LIST, "Account Switch, code = ${it.crewCode}")
        getRoster()
      }) { error -> loggingManager.logError(error) }
  }

  private fun getRoster() {
    val account = accountManager.getCurrentAccount()
    if (account.crewCode.isEmpty()) {
      rosterMonths.clear()
      rosterMonthsSubject.onNext(rosterMonths)
      screenState.onNext(ScreenState.Success)
      loggingManager.logMessage(LoggingFlow.ROSTER_LIST, "Clear roster")
      return
    }

    val months = mutableListOf<DateTime>()
    val monthStartTime = DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay()
    months.add(monthStartTime)

    for (i in 1 until 13) {
      val nextMonth = monthStartTime.plusMonths(i)
      months.add(nextMonth)
    }

    getMonthsInOrder(account, months)
  }

  private fun getMonthsInOrder(
    account: Account,
    months: MutableList<DateTime>
  ) {
    if (months.isNotEmpty()) {
      var fetchMonthsObservable = rosterRepository
        .getRosterMonth(
          crewCode = account.crewCode,
          month = months[0]
        )
        .toObservable()

      for (i in 1 until months.size) {
        fetchMonthsObservable = fetchMonthsObservable
          .concatWith(rosterRepository.getRosterMonth(
            crewCode = account.crewCode,
            month = months[i])
          )
      }

      disposables + fetchMonthsObservable
        .subscribeOn(Schedulers.io())
        .doOnSubscribe {
          rosterMonths.clear()
          screenState.onNext(ScreenState.Loading())
        }
        .subscribe({ rosterMonth ->
          loggingManager.logMessage(LoggingFlow.ROSTER_LIST, "${rosterMonth.rosterDates.size} dates")
          if (rosterMonth.rosterDates.isNotEmpty()) {
            rosterMonths.add(rosterMonth)
          }
        }, { error ->
          loggingManager.logError(error)
          screenState.onNext(ScreenState.Error())
        }, {
          loggingManager.logMessage(LoggingFlow.ROSTER_LIST, "${rosterMonths.size} months")
          rosterMonthsSubject.onNext(rosterMonths)
          screenState.onNext(ScreenState.Success)
        })
    }
  }
}