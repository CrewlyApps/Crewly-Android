package com.crewly.roster.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.account.AccountManager
import com.crewly.app.RxModule
import com.crewly.persistence.account.Account
import com.crewly.logging.LoggingFlow
import com.crewly.logging.LoggingManager
import com.crewly.models.ScreenState
import com.crewly.models.roster.RosterPeriod
import com.crewly.roster.RosterManager
import com.crewly.roster.RosterRepository
import com.crewly.utils.plus
import com.crewly.viewmodel.ScreenStateViewModel
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 04/08/2018
 */
class RosterListViewModel @Inject constructor(
  application: Application,
  private val accountManager: AccountManager,
  private val loggingManager: LoggingManager,
  private val rosterManager: RosterManager,
  private val rosterRepository: RosterRepository,
  @Named(RxModule.IO_THREAD) private val ioThread: Scheduler
):
  AndroidViewModel(application), ScreenStateViewModel {

  override val screenState = BehaviorSubject.create<ScreenState>()
  private val rosterMonthsSubject = BehaviorSubject.create<List<RosterPeriod.RosterMonth>>()

  private val rosterMonths = mutableListOf<RosterPeriod.RosterMonth>()
  private val disposables = CompositeDisposable()

  var showingEmptyView = false

  init {
    fetchRoster()
    observeRosterUpdates()
    observeAccountUpdates()
  }

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeRosterMonths(): Observable<List<RosterPeriod.RosterMonth>> =
    rosterMonthsSubject.hide()

  private fun observeRosterUpdates() {
    disposables + rosterManager
      .observeRosterUpdates()
      .subscribe({
        if (accountManager.getCurrentAccount().crewCode.isNotEmpty()) {
          loggingManager.logMessage(LoggingFlow.ROSTER_LIST, "Roster Update Observed")
          fetchRoster()
        }
      }) { error -> loggingManager.logError(error) }
  }

  private fun observeAccountUpdates() {
    disposables + accountManager
      .observeAccountSwitchEvents()
      .subscribe({
        loggingManager.logMessage(LoggingFlow.ROSTER_LIST, "Account Switch, code = ${it.crewCode}")
        fetchRoster()
      }) { error -> loggingManager.logError(error) }
  }

  private fun fetchRoster() {
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

    fetchMonthsInOrder(account, months)
  }

  private fun fetchMonthsInOrder(
    account: Account,
    months: MutableList<DateTime>
  ) {
    if (months.isNotEmpty()) {
      var fetchMonthsObservable = rosterRepository
        .fetchRosterMonth(
          crewCode = account.crewCode,
          month = months[0]
        )
        .toObservable()

      for (i in 1 until months.size) {
        fetchMonthsObservable = fetchMonthsObservable
          .concatWith(rosterRepository.fetchRosterMonth(
            crewCode = account.crewCode,
            month = months[i])
          )
      }

      disposables + fetchMonthsObservable
        .subscribeOn(ioThread)
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