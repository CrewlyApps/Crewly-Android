package com.crewly.roster.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.account.AccountManager
import com.crewly.logging.Logger
import com.crewly.views.ScreenState
import com.crewly.models.account.Account
import com.crewly.models.account.CrewType
import com.crewly.models.roster.RosterPeriod
import com.crewly.repositories.FetchRosterUseCase
import com.crewly.roster.RosterManager
import com.crewly.repositories.RosterRepository
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
 * Created by Derek on 04/08/2018
 */
class RosterListViewModel @Inject constructor(
  application: Application,
  private val accountManager: AccountManager,
  private val rosterManager: RosterManager,
  private val rosterRepository: RosterRepository,
  private val fetchRosterUseCase: FetchRosterUseCase
):
  AndroidViewModel(application), ScreenStateViewModel {

  data class RefreshRosterData(
    val crewCode: String,
    val password: String
  )

  override val screenState = BehaviorSubject.create<ScreenState>()
  private val rosterMonthsSubject = BehaviorSubject.create<List<RosterPeriod.RosterMonth>>()
  private val refreshRosterInputEvents = PublishSubject.create<RefreshRosterData>()

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

  fun observeRefreshRosterInputEvents(): Observable<RefreshRosterData> =
    refreshRosterInputEvents.hide()

  fun handleRefreshRoster() {
    val username = accountManager.getCurrentAccount().crewCode

    disposables + accountManager.getPassword(
      crewCode = username
    )
      .subscribeOn(Schedulers.io())
      .subscribe({ password ->
        refreshRosterInputEvents.onNext(
          RefreshRosterData(
            crewCode = username,
            password = password
          )
        )
      }, { error ->
        Logger.logError(error)
        screenState.onNext(ScreenState.Error("Failed to refresh roster"))
      })
  }

  fun refreshRoster(
    password: String
  ) {
    val username = accountManager.getCurrentAccount().crewCode
    val companyId = accountManager.getCurrentAccount().company.id
    val crewType = CrewType.fromType(accountManager.getCurrentAccount().crewType)

    disposables + fetchRosterUseCase.fetchRoster(
      username = username,
      password = password,
      companyId = companyId,
      crewType = crewType
    )
      .flatMap { data ->
        accountManager.updateAccount(
          account = accountManager.getCurrentAccount().copy(
            base = data.userBase
          )
        )
      }
      .flatMapCompletable {
        accountManager.savePassword(password)
      }
      .doOnSubscribe { screenState.onNext(ScreenState.Loading()) }
      .subscribeOn(Schedulers.io())
      .subscribe({
        getRoster()
      }, { error ->
        val message = error.message ?: "Failed to refresh roster"
        Logger.logError(error)
        screenState.onNext(ScreenState.Error(message))
      })
  }

  private fun observeRosterUpdates() {
    disposables + rosterManager
      .observeRosterUpdates()
      .subscribe({
        if (accountManager.getCurrentAccount().crewCode.isNotEmpty()) {
          Logger.logDebug("Roster Update Observed")
          getRoster()
        }
      }) { error -> Logger.logError(error) }
  }

  private fun observeAccountUpdates() {
    disposables + accountManager
      .observeAccountSwitchEvents()
      .subscribe({
        Logger.logDebug("Account Switch, code = ${it.crewCode}")
        getRoster()
      }) { error -> Logger.logError(error) }
  }

  private fun getRoster() {
    val account = accountManager.getCurrentAccount()
    if (account.crewCode.isEmpty()) {
      rosterMonths.clear()
      rosterMonthsSubject.onNext(rosterMonths)
      screenState.onNext(ScreenState.Success)
      Logger.logDebug("Clear roster")
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
      val fetchMonthsObservables = mutableListOf<Observable<RosterPeriod.RosterMonth>>().apply {
        for (i in 0 until months.size) {
          val fetchMonthsObservable = rosterRepository.observeRosterMonth(
            crewCode = account.crewCode,
            month = months[i]
          )

          add(fetchMonthsObservable)
        }
      }

      disposables + Observable.combineLatest(fetchMonthsObservables) {
        it.toList() as List<RosterPeriod.RosterMonth>
      }
        .distinctUntilChanged()
        .subscribeOn(Schedulers.io())
        .doOnNext {
          rosterMonths.clear()
          screenState.onNext(ScreenState.Loading())
        }
        .subscribe({
          it.forEach { rosterMonth ->
            Logger.logDebug("${rosterMonth.rosterDates.size} dates")
            if (rosterMonth.rosterDates.isNotEmpty()) {
              rosterMonths.add(rosterMonth)
            }
          }

          rosterMonthsSubject.onNext(rosterMonths)
          screenState.onNext(ScreenState.Success)

        },{ error ->
          Logger.logError(error)
          screenState.onNext(ScreenState.Error())
        })
    }
  }
}